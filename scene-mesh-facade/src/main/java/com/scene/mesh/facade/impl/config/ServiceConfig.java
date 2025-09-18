package com.scene.mesh.facade.impl.config;

import com.scene.mesh.foundation.spec.cache.ICache;
import com.scene.mesh.model.terminal.TerminalRepository;
import com.scene.mesh.service.impl.event.DefaultMetaEventService;
import com.scene.mesh.service.impl.scene.DefaultSceneService;
import com.scene.mesh.service.impl.speech.DefaultSpeechService;
import com.scene.mesh.service.impl.speech.VoskSttProcessor;
import com.scene.mesh.service.spec.cache.MutableCacheService;
import com.scene.mesh.service.spec.event.IMetaEventService;
import com.scene.mesh.service.spec.product.IProductService;
import com.scene.mesh.service.spec.scene.ISceneService;
import com.scene.mesh.service.spec.speech.AudioProcessorManager;
import com.scene.mesh.service.spec.speech.ISpeechService;
import com.scene.mesh.service.spec.terminal.ITerminalService;
import com.scene.mesh.service.impl.product.DefaultProductService;
import com.scene.mesh.service.impl.terminal.DefaultTerminalService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.vosk.Model;

import java.io.IOException;
import java.util.List;

@Configuration
public class ServiceConfig {

    @Value("${audio.model.vosk}")
    private String voskModel;

    @Bean
    public IProductService productService(MutableCacheService mutableCacheService) {
        return new DefaultProductService(mutableCacheService);
    }

    @Bean
    public ITerminalService terminalService(TerminalRepository terminalRepository){
        return new DefaultTerminalService(terminalRepository);
    }

    @Bean
    public IMetaEventService metaEventService(MutableCacheService mutableCacheService) {
        return new DefaultMetaEventService(mutableCacheService);
    }

    @Bean
    public ISceneService sceneService(MutableCacheService mutableCacheService) {
        return new DefaultSceneService(mutableCacheService);
    }

    @Bean
    public MutableCacheService mutableCacheService(ICache cache) {
        return new MutableCacheService(cache,null);
    }

    @Bean
    public AudioProcessorManager audioProcessorManager() {
        AudioProcessorManager audioProcessorManager = new AudioProcessorManager();
        Model model = null;
        try {
            model = new Model(voskModel);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        audioProcessorManager.registerSttProcessor(new VoskSttProcessor(model));
        return audioProcessorManager;
    }

    @Bean
    public ISpeechService speechService(
            AudioProcessorManager audioProcessorManager,
            IProductService productService,
            ITerminalService terminalService) {
        return new DefaultSpeechService(audioProcessorManager,productService,terminalService);
    }

}
