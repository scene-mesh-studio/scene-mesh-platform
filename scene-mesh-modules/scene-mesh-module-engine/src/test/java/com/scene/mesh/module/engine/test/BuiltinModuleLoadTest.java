package com.scene.mesh.module.engine.test;

import com.scene.mesh.module.engine.impl.SmModuleManager;
import com.scene.mesh.module.engine.impl.extension.SmExtensionManager;
import com.scene.mesh.module.engine.impl.extension.SmExtensionInvoker;
import com.scene.mesh.module.engine.spec.ISmModule;
import com.scene.mesh.module.engine.spec.ISmModuleLoader;
import com.scene.mesh.module.engine.spec.extension.ISmExtensionManager;
import com.scene.mesh.module.engine.spec.extension.ISmExtensionPlugin;
import com.scene.mesh.module.engine.spec.extension.ISmExtensionInvoker;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;

import java.io.File;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 测试加载 scene-mesh-module-builtin JAR 包
 */
@Slf4j
public class BuiltinModuleLoadTest {

    private ISmExtensionManager extensionManager;
    private SmModuleManager moduleManager;
    private ISmExtensionInvoker extensionInvoker;
    private String builtinJarPath;

    @BeforeEach
    void setUp() {
        extensionManager = new SmExtensionManager();
        moduleManager = new SmModuleManager(extensionManager);
        extensionInvoker = new SmExtensionInvoker(extensionManager);
        
        // 构建 builtin 模块 JAR 包的路径
        // 假设 JAR 包在 target 目录下
        builtinJarPath = "/Users/fang/develop/project/org/scene-mesh-platform/scene-mesh-modules/scene-mesh-module-builtin/target/scene-mesh-module-builtin-1.0.0-SNAPSHOT.jar";
        
        log.info("Builtin JAR path: {}", builtinJarPath);
        
        // 验证 JAR 文件是否存在
        File jarFile = new File(builtinJarPath);
        assertTrue(jarFile.exists(), "Builtin JAR file should exist at: " + builtinJarPath);
        log.info("JAR file size: {} bytes", jarFile.length());
    }

    @AfterEach
    void tearDown() {
        if (moduleManager != null) {
            try {
                // 清理所有已加载的模块
                moduleManager.unloadModule("scene-mesh-builtin");
            } catch (Exception e) {
                log.warn("Failed to unload modules during cleanup", e);
            }
        }
    }

    @Test
    void testLoadBuiltinModule() throws Exception {
        log.info("Starting testLoadBuiltinModule");
        
        // 加载 builtin 模块
        String moduleId = "scene-mesh-builtin";
        moduleManager.loadModule(moduleId, builtinJarPath);
        
        // 验证模块是否成功加载
        assertTrue(moduleManager.isModuleLoaded(moduleId), "Module should be loaded");
        
        // 获取模块信息
        ISmModule module = moduleManager.getModule(moduleId);
        assertNotNull(module, "Module should not be null");
        
        // 验证模块基本信息（与 MANIFEST.MF 保持一致）
        assertEquals("scene-mesh-builtin", module.getId(), "Module ID should match MANIFEST.MF");
        assertEquals("Scene Mesh Builtin Module", module.getName(), "Module name should match MANIFEST.MF");
        assertEquals("1.0.0", module.getVersion(), "Module version should match MANIFEST.MF");
        assertEquals(builtinJarPath, module.getPath(), "Module path should match");
        
        // 验证模块元数据（从 MANIFEST.MF 中读取的信息）
        Map<String, Object> metas = module.getMetas();
        assertNotNull(metas, "Module metas should not be null");
        assertTrue(metas.containsKey("jarFile"), "Module metas should contain jarFile");
        assertTrue(metas.containsKey("jarSize"), "Module metas should contain jarSize");
        assertTrue(metas.containsKey("loadTime"), "Module metas should contain loadTime");
        assertTrue(metas.containsKey("hasManifest"), "Module metas should contain hasManifest");
        assertTrue(metas.containsKey("moduleDescription"), "Module metas should contain moduleDescription");
        assertTrue(metas.containsKey("moduleAuthor"), "Module metas should contain moduleAuthor");
        
        // 验证元数据值
        assertEquals(builtinJarPath, metas.get("jarFile"), "jarFile should match");
        assertTrue((Boolean) metas.get("hasManifest"), "hasManifest should be true");
        assertEquals("Built-in module for Scene Mesh Platform", metas.get("moduleDescription"), "moduleDescription should match MANIFEST.MF");
        assertEquals("Scene Mesh Team", metas.get("moduleAuthor"), "moduleAuthor should match MANIFEST.MF");
        
        log.info("Module loaded successfully: {}", module);
        log.info("Module metas: {}", metas);
    }

    @Test
    void testExtensionPluginScanning() throws Exception {
        log.info("Starting testExtensionPluginScanning");
        
        // 加载 builtin 模块
        String moduleId = "scene-mesh-builtin";
        moduleManager.loadModule(moduleId, builtinJarPath);
        
        // 验证模块是否成功加载
        assertTrue(moduleManager.isModuleLoaded(moduleId), "Module should be loaded");
        
        // 获取模块信息
        ISmModule module = moduleManager.getModule(moduleId);
        assertNotNull(module, "Module should not be null");
        
        // 验证扩展插件是否被正确注册
        List<ISmExtensionPlugin> modulePlugins = extensionManager.findPluginsByModule(moduleId);
        assertNotNull(modulePlugins, "Module plugins list should not be null");
        log.info("Found {} plugins for module {}", modulePlugins.size(), moduleId);
        
        // 验证具体的插件
        assertTrue(modulePlugins.size() >= 4, "Should have at least 4 plugins (SttCalculator, TtsCalculator, RequiredParameterValidator, FormatValidator)");
        
        // 验证 calculate 槽位的插件
        List<ISmExtensionPlugin> calculatePlugins = extensionManager.findPluginsBySlot("calculate");
        assertTrue(calculatePlugins.size() >= 2, "Should have at least 2 calculate plugins");
        
        // 验证 validator 槽位的插件
        List<ISmExtensionPlugin> validatorPlugins = extensionManager.findPluginsBySlot("validator");
        assertTrue(validatorPlugins.size() >= 2, "Should have at least 2 validator plugins");
        
        // 验证具体插件ID
        ISmExtensionPlugin sttPlugin = extensionManager.findExtensionPlugin("stt");
        assertNotNull(sttPlugin, "STT plugin should be registered");
        assertEquals("stt", sttPlugin.getId(), "STT plugin ID should match");
        assertEquals("calculate", sttPlugin.getExtensionSlotId(), "STT plugin slot should be calculate");
        
        ISmExtensionPlugin ttsPlugin = extensionManager.findExtensionPlugin("tts");
        assertNotNull(ttsPlugin, "TTS plugin should be registered");
        assertEquals("tts", ttsPlugin.getId(), "TTS plugin ID should match");
        assertEquals("calculate", ttsPlugin.getExtensionSlotId(), "TTS plugin slot should be calculate");
        
        ISmExtensionPlugin requiredValidatorPlugin = extensionManager.findExtensionPlugin("required");
        assertNotNull(requiredValidatorPlugin, "Required validator plugin should be registered");
        assertEquals("required", requiredValidatorPlugin.getId(), "Required validator plugin ID should match");
        assertEquals("validator", requiredValidatorPlugin.getExtensionSlotId(), "Required validator plugin slot should be validator");
        
        ISmExtensionPlugin formatValidatorPlugin = extensionManager.findExtensionPlugin("format");
        assertNotNull(formatValidatorPlugin, "Format validator plugin should be registered");
        assertEquals("format", formatValidatorPlugin.getId(), "Format validator plugin ID should match");
        assertEquals("validator", formatValidatorPlugin.getExtensionSlotId(), "Format validator plugin slot should be validator");
        
        log.info("Extension plugin scanning test completed successfully - found {} plugins", modulePlugins.size());
    }

    @Test
    void testModuleReload() throws Exception {
        log.info("Starting testModuleReload");
        
        // 加载 builtin 模块
        String moduleId = "scene-mesh-builtin";
        moduleManager.loadModule(moduleId, builtinJarPath);
        
        // 验证模块已加载
        assertTrue(moduleManager.isModuleLoaded(moduleId), "Module should be loaded");
        
        // 重新加载模块
        moduleManager.reloadModule(moduleId);
        
        // 验证模块仍然加载
        assertTrue(moduleManager.isModuleLoaded(moduleId), "Module should still be loaded after reload");
        
        // 验证模块信息仍然正确
        ISmModule module = moduleManager.getModule(moduleId);
        assertNotNull(module, "Module should not be null after reload");
        assertEquals("scene-mesh-builtin", module.getId(), "Module ID should still match after reload");
        
        log.info("Module reload test completed successfully");
    }

    @Test
    void testModuleUnload() throws Exception {
        log.info("Starting testModuleUnload");
        
        // 加载 builtin 模块
        String moduleId = "scene-mesh-builtin";
        moduleManager.loadModule(moduleId, builtinJarPath);
        
        // 验证模块已加载
        assertTrue(moduleManager.isModuleLoaded(moduleId), "Module should be loaded");
        
        // 卸载模块
        moduleManager.unloadModule(moduleId);
        
        // 验证模块已卸载
        assertFalse(moduleManager.isModuleLoaded(moduleId), "Module should be unloaded");
        
        // 验证无法获取模块
        ISmModule module = moduleManager.getModule(moduleId);
        assertNull(module, "Module should be null after unload");
        
        log.info("Module unload test completed successfully");
    }

    @Test
    void testSttCalculatorInvocation() throws Exception {
        log.info("Starting testSttCalculatorInvocation");
        
        // 加载 builtin 模块
        String moduleId = "scene-mesh-builtin";
        moduleManager.loadModule(moduleId, builtinJarPath);
        
        // 验证STT插件已注册
        ISmExtensionPlugin sttPlugin = extensionManager.findExtensionPlugin("stt");
        assertNotNull(sttPlugin, "STT plugin should be registered");
        
        // 准备测试数据
        java.util.Map<String, Object> payload = new java.util.HashMap<>();
        payload.put("audioUrl", "https://example.com/audio/test.wav");
        payload.put("language", "zh-CN");
        payload.put("model", "default");
        payload.put("enablePunctuation", true);
        payload.put("enableWordTimestamps", false);
        
        // 使用ISmExtensionInvoker调用STT计算器
        extensionInvoker.invokeExtension("stt", "calculate", payload);
        
        // 验证计算结果
        assertTrue(payload.containsKey("text"), "STT result should contain text");
        assertTrue(payload.containsKey("confidence"), "STT result should contain confidence");
        assertTrue(payload.containsKey("duration"), "STT result should contain duration");
        assertTrue(payload.containsKey("languageDetected"), "STT result should contain languageDetected");
        assertTrue(payload.containsKey("wordCount"), "STT result should contain wordCount");
        
        // 验证结果值
        assertEquals("这是模拟的语音识别结果文本", payload.get("text"), "STT text should match expected result");
        assertEquals(0.95, (Double) payload.get("confidence"), 0.01, "STT confidence should match expected result");
        assertEquals(5.2, (Double) payload.get("duration"), 0.01, "STT duration should match expected result");
        assertEquals("zh-CN", payload.get("languageDetected"), "STT language should match input");
        assertEquals(13, payload.get("wordCount"), "STT word count should match text length");
        
        log.info("STT calculator invocation test completed successfully");
    }

    @Test
    void testTtsCalculatorInvocation() throws Exception {
        log.info("Starting testTtsCalculatorInvocation");
        
        // 加载 builtin 模块
        String moduleId = "scene-mesh-builtin";
        moduleManager.loadModule(moduleId, builtinJarPath);
        
        // 验证TTS插件已注册
        ISmExtensionPlugin ttsPlugin = extensionManager.findExtensionPlugin("tts");
        assertNotNull(ttsPlugin, "TTS plugin should be registered");
        
        // 准备测试数据
        java.util.Map<String, Object> payload = new java.util.HashMap<>();
        payload.put("text", "Hello, this is a test message");
        payload.put("voice", "female");
        payload.put("language", "en-US");
        payload.put("speed", 1.2);
        payload.put("pitch", 1.1);
        payload.put("volume", 0.9);
        
        // 使用ISmExtensionInvoker调用TTS计算器
        extensionInvoker.invokeExtension("tts", "calculate", payload);
        
        // 验证计算结果
        assertTrue(payload.containsKey("audioUrl"), "TTS result should contain audioUrl");
        assertTrue(payload.containsKey("duration"), "TTS result should contain duration");
        assertTrue(payload.containsKey("processedText"), "TTS result should contain processedText");
        assertTrue(payload.containsKey("voiceUsed"), "TTS result should contain voiceUsed");
        assertTrue(payload.containsKey("languageUsed"), "TTS result should contain languageUsed");
        
        // 验证结果值
        assertTrue(((String) payload.get("audioUrl")).startsWith("https://api.example.com/audio/"), 
                   "TTS audioUrl should be a valid URL");
        assertTrue((Double) payload.get("duration") > 0, "TTS duration should be positive");
        assertEquals("Hello, this is a test message", payload.get("processedText"), "TTS processed text should match input");
        assertEquals("female", payload.get("voiceUsed"), "TTS voice should match input");
        assertEquals("en-US", payload.get("languageUsed"), "TTS language should match input");
        
        log.info("TTS calculator invocation test completed successfully");
    }

    @Test
    void testFormatValidatorInvocation() throws Exception {
        log.info("Starting testFormatValidatorInvocation");
        
        // 加载 builtin 模块
        String moduleId = "scene-mesh-builtin";
        moduleManager.loadModule(moduleId, builtinJarPath);
        
        // 验证格式验证器插件已注册
        ISmExtensionPlugin formatPlugin = extensionManager.findExtensionPlugin("format");
        assertNotNull(formatPlugin, "Format validator plugin should be registered");
        
        // 测试有效数据
        java.util.Map<String, Object> validPayload = new java.util.HashMap<>();
        validPayload.put("audioUrl", "https://example.com/audio/test.wav");
        validPayload.put("email", "test@example.com");
        validPayload.put("language", "zh-CN");
        validPayload.put("speed", 1.0);
        validPayload.put("pitch", 1.0);
        validPayload.put("volume", 0.8);
        
        // 使用ISmExtensionInvoker调用格式验证器
        Boolean result = (Boolean) extensionInvoker.invokeExtension("format", "validate", validPayload);
        assertTrue(result, "Format validation should pass for valid data");
        
        // 测试无效URL
        java.util.Map<String, Object> invalidUrlPayload = new java.util.HashMap<>();
        invalidUrlPayload.put("audioUrl", "invalid-url");
        invalidUrlPayload.put("email", "test@example.com");
        invalidUrlPayload.put("language", "zh-CN");
        
        result = (Boolean) extensionInvoker.invokeExtension("format", "validate", invalidUrlPayload);
        assertFalse(result, "Format validation should fail for invalid URL");
        
        // 测试无效邮箱
        java.util.Map<String, Object> invalidEmailPayload = new java.util.HashMap<>();
        invalidEmailPayload.put("audioUrl", "https://example.com/audio/test.wav");
        invalidEmailPayload.put("email", "invalid-email");
        invalidEmailPayload.put("language", "zh-CN");
        
        result = (Boolean) extensionInvoker.invokeExtension("format", "validate", invalidEmailPayload);
        assertFalse(result, "Format validation should fail for invalid email");
        
        // 测试无效语言代码
        java.util.Map<String, Object> invalidLanguagePayload = new java.util.HashMap<>();
        invalidLanguagePayload.put("audioUrl", "https://example.com/audio/test.wav");
        invalidLanguagePayload.put("email", "test@example.com");
        invalidLanguagePayload.put("language", "invalid-language");
        
        result = (Boolean) extensionInvoker.invokeExtension("format", "validate", invalidLanguagePayload);
        assertFalse(result, "Format validation should fail for invalid language code");
        
        // 测试无效数值范围
        java.util.Map<String, Object> invalidRangePayload = new java.util.HashMap<>();
        invalidRangePayload.put("audioUrl", "https://example.com/audio/test.wav");
        invalidRangePayload.put("email", "test@example.com");
        invalidRangePayload.put("language", "zh-CN");
        invalidRangePayload.put("speed", 5.0); // 超出范围
        
        result = (Boolean) extensionInvoker.invokeExtension("format", "validate", invalidRangePayload);
        assertFalse(result, "Format validation should fail for invalid speed range");
        
        log.info("Format validator invocation test completed successfully");
    }

    @Test
    void testRequiredValidatorInvocation() throws Exception {
        log.info("Starting testRequiredValidatorInvocation");
        
        // 加载 builtin 模块
        String moduleId = "scene-mesh-builtin";
        moduleManager.loadModule(moduleId, builtinJarPath);
        
        // 验证必需参数验证器插件已注册
        ISmExtensionPlugin requiredPlugin = extensionManager.findExtensionPlugin("required");
        assertNotNull(requiredPlugin, "Required validator plugin should be registered");
        
        // 测试有效数据（包含所有必需参数）
        java.util.Map<String, Object> validPayload = new java.util.HashMap<>();
        validPayload.put("text", "Hello world");
        validPayload.put("audioUrl", "https://example.com/audio/test.wav");
        
        // 使用ISmExtensionInvoker调用必需参数验证器
        Boolean result = (Boolean) extensionInvoker.invokeExtension("required", "validate", validPayload);
        assertTrue(result, "Required validation should pass for valid data");
        
        // 测试缺少必需参数
        java.util.Map<String, Object> missingTextPayload = new java.util.HashMap<>();
        missingTextPayload.put("audioUrl", "https://example.com/audio/test.wav");
        // 缺少 text 参数
        
        result = (Boolean) extensionInvoker.invokeExtension("required", "validate", missingTextPayload);
        assertFalse(result, "Required validation should fail for missing text parameter");
        
        // 测试缺少audioUrl参数
        java.util.Map<String, Object> missingAudioPayload = new java.util.HashMap<>();
        missingAudioPayload.put("text", "Hello world");
        // 缺少 audioUrl 参数
        
        result = (Boolean) extensionInvoker.invokeExtension("required", "validate", missingAudioPayload);
        assertFalse(result, "Required validation should fail for missing audioUrl parameter");
        
        // 测试空字符串参数
        java.util.Map<String, Object> emptyTextPayload = new java.util.HashMap<>();
        emptyTextPayload.put("text", ""); // 空字符串
        emptyTextPayload.put("audioUrl", "https://example.com/audio/test.wav");
        
        result = (Boolean) extensionInvoker.invokeExtension("required", "validate", emptyTextPayload);
        assertFalse(result, "Required validation should fail for empty text parameter");
        
        // 测试null参数
        java.util.Map<String, Object> nullTextPayload = new java.util.HashMap<>();
        nullTextPayload.put("text", null); // null值
        nullTextPayload.put("audioUrl", "https://example.com/audio/test.wav");
        
        result = (Boolean) extensionInvoker.invokeExtension("required", "validate", nullTextPayload);
        assertFalse(result, "Required validation should fail for null text parameter");
        
        log.info("Required validator invocation test completed successfully");
    }

    @Test
    void testPluginInvocationWithErrorHandling() throws Exception {
        log.info("Starting testPluginInvocationWithErrorHandling");
        
        // 加载 builtin 模块
        String moduleId = "scene-mesh-builtin";
        moduleManager.loadModule(moduleId, builtinJarPath);
        
        // 测试STT计算器错误处理
        ISmExtensionPlugin sttPlugin = extensionManager.findExtensionPlugin("stt");
        assertNotNull(sttPlugin, "STT plugin should be registered");
        
        // 测试缺少必需参数的情况
        java.util.Map<String, Object> invalidPayload = new java.util.HashMap<>();
        // 故意不提供 audioUrl 参数
        
        try {
            extensionInvoker.invokeExtension("stt", "calculate", invalidPayload);
            fail("STT calculator should throw exception for missing audioUrl");
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("AudioUrl parameter is required"), 
                      "Exception message should indicate missing audioUrl parameter");
            log.info("STT calculator correctly handled missing parameter: {}", e.getMessage());
        }
        
        // 测试TTS计算器错误处理
        ISmExtensionPlugin ttsPlugin = extensionManager.findExtensionPlugin("tts");
        assertNotNull(ttsPlugin, "TTS plugin should be registered");
        
        // 测试缺少必需参数的情况
        java.util.Map<String, Object> invalidTtsPayload = new java.util.HashMap<>();
        // 故意不提供 text 参数
        
        try {
            extensionInvoker.invokeExtension("tts", "calculate", invalidTtsPayload);
            fail("TTS calculator should throw exception for missing text");
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("Text parameter is required"), 
                      "Exception message should indicate missing text parameter");
            log.info("TTS calculator correctly handled missing parameter: {}", e.getMessage());
        }
        
        log.info("Plugin invocation error handling test completed successfully");
    }
}
