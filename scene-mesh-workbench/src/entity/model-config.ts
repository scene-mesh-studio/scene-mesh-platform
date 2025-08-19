import { SceneFlowEditorContainer } from "@/viewpoints/scene/views/editor";
import type {
  IEntityView,
  IEntityModel,
  IEntityViewMenuItem,
} from "@scenemesh/entity-engine";

import { z as zod } from "zod";

export const models: IEntityModel[] = [
  {
    name: "event",
    title: "事件",
    description: "事件",
    fields: [
      { name: "name", title: "事件名称", type: "string", isRequired: true },
      { name: "title", title: "事件标题", type: "string", isRequired: true },
      {
        name: "description",
        title: "事件说明",
        type: "string",
        isRequired: true,
      },
      {
        name: "fields",
        title: "事件字段",
        type: "one_to_many",
        refModel: "eventField",
      },
    ],
  },
  {
    name: "eventField",
    title: "事件字段",
    description: "事件字段",
    fields: [
      {
        name: "fieldName",
        title: "字段名称",
        type: "string",
        isRequired: true,
      },
      {
        name: "fieldTitle",
        title: "字段标题",
        type: "string",
        isRequired: true,
      },
      {
        name: "fieldCategory",
        title: "字段类别",
        type: "enum",
        typeOptions: {
          options: [
            { value: "native", label: "原生字段" },
            { value: "compute", label: "计算字段" },
          ],
        },
        isRequired: true,
      },
      {
        name: "fieldType",
        title: "数据类型",
        type: "enum",
        typeOptions: {
          options: [
            { value: "string", label: "字符串" },
            { value: "number", label: "数字" },
            { value: "boolean", label: "布尔" },
            { value: "datetime", label: "日期时间" },
            { value: "binary", label: "二进制" },
            { value: "json", label: "JSON" },
            { value: "array", label: "数组" },
          ],
        },
        isRequired: true,
      },
      { name: "fieldAsInput", title: "字段作为输入", type: "boolean" },
      { name: "fieldDescription", title: "字段描述", type: "string" },
    ],
  },
  {
    name: "action",
    title: "动作",
    description: "动作",
    fields: [
      { name: "name", title: "动作名称", type: "string", isRequired: true },
      { name: "title", title: "动作标题", type: "string", isRequired: true },
      {
        name: "description",
        title: "动作说明",
        type: "string",
        isRequired: true,
      },
      {
        name: "fields",
        title: "动作字段",
        type: "one_to_many",
        refModel: "actionField",
      },
    ],
  },
  {
    name: "actionField",
    title: "动作字段",
    description: "动作字段",
    fields: [
      {
        name: "fieldName",
        title: "字段名称",
        type: "string",
        isRequired: true,
      },
      {
        name: "fieldTitle",
        title: "字段标题",
        type: "string",
        isRequired: true,
      },
      {
        name: "fieldCategory",
        title: "字段类别",
        type: "enum",
        typeOptions: {
          options: [
            { value: "native", label: "原生字段" },
            { value: "compute", label: "计算字段" },
          ],
        },
        isRequired: true,
      },
      {
        name: "fieldType",
        title: "数据类型",
        type: "enum",
        typeOptions: {
          options: [
            { value: "string", label: "字符串" },
            { value: "number", label: "数字" },
            { value: "boolean", label: "布尔" },
            { value: "datetime", label: "日期时间" },
            { value: "binary", label: "二进制" },
            { value: "json", label: "JSON" },
            { value: "array", label: "数组" },
          ],
        },
        isRequired: true,
      },
      { name: "fieldDescription", title: "字段描述", type: "string" },
    ],
  },
  {
    name: "productSetting",
    title: "产品设置",
    description: "产品设置",
    fields: [
      { name: "secret", title: "秘钥", type: "string", isRequired: false },
      {
        name: "mqttEnabled",
        title: "启用MQTT",
        type: "boolean",
        isRequired: true,
      },
      {
        name: "mqttPort",
        title: "MQTT端口",
        type: "number",
        isRequired: false,
        defaultValue: 1883,
      },
      {
        name: "webSocketEnabled",
        title: "启用WebSocket",
        type: "boolean",
        isRequired: true,
      },
      {
        name: "webSocketPort",
        title: "WebSocket端口",
        type: "number",
        isRequired: false,
        defaultValue: 8080,
      },
    ],
  },
  {
    name: "product",
    title: "产品",
    description: "产品",
    fields: [
      {
        name: "name",
        title: "产品名称",
        type: "string",
        isRequired: true,
        searchable: true,
      },
      {
        name: "description",
        title: "产品说明",
        type: "string",
        isRequired: true,
      },
      { name: "image", title: "产品图片", type: "binary", isRequired: true },
      {
        name: "category",
        title: "产品分类",
        type: "enum",
        typeOptions: {
          options: [
            { label: "家用", value: "0" },
            { label: "办公", value: "1" },
            { label: "数码", value: "2" },
          ],
        },
        isRequired: true,
        searchable: true,
      },
      {
        name: "settings",
        title: "产品配置",
        type: "one_to_one",
        refModel: "productSetting",
      },
      {
        name: "events",
        title: "事件",
        type: "one_to_many",
        refModel: "event",
      },
      {
        name: "actions",
        title: "动作",
        type: "one_to_many",
        refModel: "action",
      },
      {
        name: "rootScene",
        title: "根场景",
        type: "one_to_one",
        refModel: "scene",
      },
    ],
  },
  {
    name: "languageModelProvider",
    title: "模型供应商",
    description: "大语言模型供应商",
    fields: [
      { name: "name", title: "供应商名称", type: "string", isRequired: true },
      {
        name: "description",
        title: "语言模型说明",
        type: "string",
        isRequired: true,
      },
      {
        name: "image",
        title: "语言模型图片",
        type: "binary",
        isRequired: true,
      },
      {
        name: "apiMode",
        title: "API模式",
        type: "enum",
        typeOptions: {
          options: [{ value: "openai", label: "OpenAI API兼容" }],
        },
        isRequired: true,
      },
      {
        name: "apiHost",
        title: "API主机",
        type: "string",
        isRequired: true,
        schema: zod.string().url(),
      },
      { name: "apiKey", title: "API密钥", type: "string", isRequired: true },
      {
        name: "apiCompatibility",
        title: "API兼容",
        type: "boolean",
        defaultValue: false,
        isRequired: true,
      },
      {
        name: "models",
        title: "大语言模型",
        type: "one_to_many",
        refModel: "languageModel",
      },
    ],
  },
  {
    name: "languageModel",
    title: "大语言模型",
    description: "大语言模型",
    fields: [
      {
        name: "name",
        title: "模型名称",
        type: "string",
        isRequired: true,
        searchable: true,
      },
      {
        name: "description",
        title: "模型说明",
        type: "string",
        isRequired: false,
      },
      {
        name: "feature",
        title: "模型特性",
        type: "array",
        typeOptions: {
          options: [
            { value: "vision", label: "视觉" },
            { value: "internet", label: "联网" },
            { value: "embedding", label: "嵌入" },
            { value: "reason", label: "推理" },
            { value: "function", label: "工具" },
            { value: "tts", label: "语音合成" },
            { value: "stt", label: "语音转录" },
          ],
        },
        isRequired: true,
        searchable: true,
      },
    ],
  },
  {
    name: "mcpService",
    title: "MCP服务",
    description: "MCP服务配置",
    fields: [
      { name: "name", title: "MCP服务名称", type: "string", isRequired: true },
      {
        name: "description",
        title: "MCP服务说明",
        type: "string",
        isRequired: false,
      },
      {
        name: "type",
        title: "服务类型",
        type: "enum",
        isRequired: true,
        searchable: true,
        typeOptions: {
          options: [
            { value: "sse", label: "服务端发送事件(sse)" },
            { value: "streamable", label: "流式传输HTTP" },
          ],
        },
      },
      {
        name: "url",
        title: "服务地址",
        type: "string",
        isRequired: true,
        schema: zod.string().url(),
      },
      { name: "header", title: "请求头", type: "string", isRequired: false },
      {
        name: "timeout",
        title: "超时",
        description: "单位:秒",
        type: "number",
        isRequired: false,
        defaultValue: 60,
      },
      {
        name: "enable",
        title: "是否启用",
        type: "boolean",
        isRequired: true,
        defaultValue: true,
      },
    ],
  },
  {
    name: "scene",
    title: "场景",
    description: "场景",
    fields: [
      {
        name: "name",
        title: "场景名称",
        type: "string",
        isRequired: true,
        searchable: true,
      },
      {
        name: "description",
        title: "场景说明",
        type: "string",
        isRequired: false,
        searchable: true,
      },
      {
        name: "enable",
        title: "是否启用",
        type: "boolean",
        isRequired: true,
        defaultValue: true,
      },
      {
        name: "timeWindow",
        title: "时间窗口",
        description: "设置为0时不起用,单位:秒",
        type: "number",
        isRequired: false,
        defaultValue: 300,
      },
      {
        name: "promptInherited",
        title: "提示词继承类型(LLM)",
        type: "enum",
        typeOptions: {
          options: [
            { value: "inherit", label: "继承父场景提示词" },
            { value: "overwrite", label: "不继承父场景提示词" },
          ],
        },
        isRequired: true,
        searchable: true,
        defaultValue: "inherit",
      },
      {
        name: "prompt",
        title: "提示词(LLM)",
        type: "string",
        isRequired: true,
      },
      {
        name: "input",
        title: "输入(LLM)",
        type: "string",
        isRequired: false,
        defaultValue: "",
      },
      { name: "rules", title: "规则", type: "json" },
      { name: "flow", title: "流程设计", type: "string" },
      { name: "flowData", title: "流程数据", type: "string" },
      {
        name: "flowDataPublishTime",
        title: "流程数据发布时间",
        type: "date",
        isRequired: false,
      },
      {
        name: "children",
        title: "子场景",
        type: "one_to_many",
        refModel: "scene",
      },
    ],
  },
];

export const views: IEntityView[] = [
  {
    name: "sceneGridView",
    title: "场景列表",
    modelName: "scene",
    description: "场景",
    viewType: "grid",
    viewOptions: {},
    items: [
      { name: "name", title: "名称", spanCols: 12 },
      { name: "description", title: "说明", spanCols: 12, flex: 1 },
      { name: "timeWindow", title: "时间窗口", spanCols: 12 },
      { name: "enable", title: "启用", spanCols: 12 },
      { name: "promptInherited", title: "提示词继承类型", spanCols: 12 },
      { name: "prompt", title: "提示词", spanCols: 12 },
      { name: "input", title: "输入", spanCols: 12 },
    ],
  },
  {
    name: "sceneFormView",
    title: "场景",
    modelName: "scene",
    description: "场景",
    viewType: "form",
    density: "medium",
    items: [
      { name: "name", title: "名称", spanCols: 12 },
      { name: "description", title: "说明", spanCols: 12, flex: 1 },
      { name: "timeWindow", title: "时间窗口", spanCols: 12 },
      { name: "enable", title: "启用", spanCols: 12 },
      { name: "promptInherited", title: "提示词继承类型", spanCols: 12 },
      { name: "prompt", title: "提示词", spanCols: 12 },
      { name: "input", title: "输入", spanCols: 12 },
      {
        name: "children",
        title: "子场景",
        widget: "references",
        width: 120,
        spanCols: 12,
      },
    ],
  },
  {
    name: "sceneFormViewSimple",
    title: "场景",
    modelName: "scene",
    description: "场景",
    viewType: "form",
    density: "medium",
    items: [
      { name: "name", title: "名称", spanCols: 12 },
      { name: "description", title: "说明", spanCols: 12, flex: 1 },
      { name: "timeWindow", title: "时间窗口", spanCols: 12 },
      { name: "enable", title: "启用", spanCols: 12 },
      { name: "promptInherited", title: "提示词继承类型", spanCols: 12 },
      { name: "prompt", title: "提示词", spanCols: 12 },
      { name: "input", title: "输入", spanCols: 12 },
    ],
  },
  {
    name: "sceneMastailView",
    title: "场景设计",
    modelName: "scene",
    description: "场景设计视图",
    viewType: "mastail",
    density: "medium",
    items: [
      {
        name: "sceneGraph",
        title: "场景图",
        widget: "graph",
        widgetOptions: {
          titleFieldName: "name",
          infoFields: ["description", "enable"],
          fromModelName: "product",
          fromFieldName: "rootScene",
          selfChildrenFieldName: "children",
          createViewName: "sceneFormViewSimple",
          editViewName: "sceneFormViewSimple",
          treeViewMode: true,
        },
        spanCols: 2,
      },
      {
        name: "panel",
        title: "面板",
        spanCols: 10,
        widgetOptions: {
          sx: {
            with: "auto",
          },
        },
        fields: [
          {
            name: "mainTab",
            title: "标签",
            spanCols: 12,
            widget: "tab",
            fields: [
              {
                name: "general",
                title: "基本信息",
                width: 200,
                widget: "action",
                icon: "material-icon-theme:settings",
                widgetOptions: {
                  actionType: "view",
                  payload: {
                    modelName: "scene",
                    viewType: "form",
                    viewName: "sceneFormViewSimple",
                  },
                },
              },
              {
                name: "design2",
                title: "场景设计",
                width: 200,
                widget: "action",
                icon: "material-icon-theme:tree",
                widgetOptions: {
                  actionType: "comp",
                  payload: {
                    comp: SceneFlowEditorContainer,
                  },
                },
              },
            ],
          },
        ],
      },
    ],
  },
  {
    name: "mcpServiceGridView",
    title: "MCP服务列表",
    modelName: "mcpService",
    description: "MCP服务",
    viewType: "grid",
    viewOptions: {},
    items: [
      { name: "name", title: "名称", spanCols: 12 },
      { name: "type", title: "类型", spanCols: 12, width: 150 },
      { name: "url", title: "地址", spanCols: 12, flex: 1 },
      { name: "timeout", title: "超时", spanCols: 12 },
      { name: "enable", title: "启用", spanCols: 12 },
      { name: "description", title: "说明", spanCols: 12, flex: 1 },
    ],
  },
  {
    name: "mcpServiceFormView",
    title: "MCP服务",
    modelName: "mcpService",
    description: "MCP服务",
    viewType: "form",
    density: "medium",
    items: [
      { name: "name", title: "名称", spanCols: 12 },
      { name: "description", title: "说明", spanCols: 12, flex: 1 },
      { name: "type", title: "类型", spanCols: 12 },
      { name: "url", title: "地址", spanCols: 12 },
      {
        name: "header",
        title: "请求头",
        widget: "textfield",
        widgetOptions: { multiline: true },
        spanCols: 12,
      },
      { name: "timeout", title: "超时", spanCols: 12 },
      { name: "enable", title: "启用", spanCols: 12 },
    ],
  },
  {
    name: "languageModelProviderGridView",
    title: "模型供应商列表",
    modelName: "languageModelProvider",
    description: "模型供应商",
    viewType: "grid",
    viewOptions: {},
    items: [
      { name: "name", title: "名称", spanCols: 12 },

      {
        name: "image",
        title: "图片",
        widget: "image",
        widgetOptions: { height: 40, width: 40 },
        width: 100,
        spanCols: 12,
      },
      { name: "apiMode", title: "API模式", spanCols: 12 },
      { name: "apiHost", title: "API主机", spanCols: 12 },
      { name: "apiCompatibility", title: "API兼容", spanCols: 12 },
      {
        name: "models",
        title: "模型列表",
        widget: "reference",
        width: 120,
        spanCols: 12,
      },
      { name: "description", title: "说明", spanCols: 12, flex: 1 },
    ],
  },
  {
    name: "languageModelProviderFormView",
    title: "模型供应商",
    modelName: "languageModelProvider",
    description: "模型供应商",
    viewType: "form",
    density: "medium",
    items: [
      { name: "name", title: "供应商名称", spanCols: 12 },
      {
        name: "description",
        title: "供应商说明",
        widget: "textfield",
        widgetOptions: { multiline: true },
        spanCols: 12,
      },
      {
        name: "image",
        title: "供应商图片",
        widget: "image",
        widgetOptions: { height: 100, width: 100 },
        spanCols: 12,
      },
      { name: "apiHost", title: "API主机", spanCols: 6 },
      { name: "apiKey", title: "API密钥", widget: "password", spanCols: 6 },
      { name: "apiMode", title: "API模式", spanCols: 6 },
      { name: "apiCompatibility", title: "API兼容", spanCols: 6 },
      {
        name: "models",
        title: "大语言模型",
        widget: "references",
        width: 120,
        spanCols: 12,
      },
    ],
  },
  {
    name: "languageModelGridView",
    title: "大语言模型列表",
    modelName: "languageModel",
    description: "大语言模型",
    viewType: "grid",
    viewOptions: {},
    items: [
      { name: "name", title: "模型名称", spanCols: 12, flex: 1 },
      { name: "description", title: "模型说明", spanCols: 12, flex: 1 },
      { name: "feature", title: "模型特性", spanCols: 12, flex: 1 },
    ],
  },
  {
    name: "languageModelFormView",
    title: "大语言模型",
    modelName: "languageModel",
    description: "大语言模型",
    viewType: "form",
    density: "medium",
    items: [
      { name: "name", title: "模型名称", spanCols: 12 },
      {
        name: "description",
        title: "模型说明",
        widget: "textfield",
        widgetOptions: { multiline: true },
        spanCols: 12,
      },
      { name: "feature", title: "模型特性", spanCols: 12 },
    ],
  },
  {
    name: "productSettingFormView",
    title: "产品设置",
    modelName: "productSetting",
    description: "产品设置",
    viewType: "form",
    density: "medium",
    items: [
      { name: "secret", title: "密钥", widget: "secret", spanCols: 12 },
      { name: "mqttEnabled", title: "启用MQTT", spanCols: 12 },
      {
        name: "mqttPort",
        title: "MQTT端口",
        spanCols: 12,
        showWhen: "mqttEnabled",
      },
      { name: "webSocketEnabled", title: "启用WebSocket", spanCols: 12 },
      {
        name: "webSocketPort",
        title: "WebSocket端口",
        spanCols: 12,
        showWhen: "webSocketEnabled",
      },
    ],
  },
  {
    name: "eventGridView",
    title: "事件列表",
    modelName: "event",
    description: "事件",
    viewType: "grid",
    viewOptions: {},
    items: [
      { name: "name", title: "事件名称", spanCols: 12 },
      { name: "title", title: "事件标题", spanCols: 12 },
      {
        name: "fields",
        title: "事件字段",
        widget: "reference",
        width: 120,
        spanCols: 12,
      },
      { name: "description", title: "事件说明", spanCols: 12, flex: 1 },
    ],
  },
  {
    name: "eventFormView",
    title: "事件",
    modelName: "event",
    description: "事件",
    viewType: "form",
    density: "medium",
    items: [
      { name: "name", title: "事件名称", spanCols: 12 },
      { name: "title", title: "事件标题", spanCols: 12 },
      {
        name: "description",
        title: "事件说明",
        spanCols: 12,
        flex: 1,
        widget: "textfield",
        widgetOptions: { multiline: true },
      },
      { name: "fields", title: "事件字段", spanCols: 12 },
    ],
  },
  {
    name: "eventFieldGridView",
    title: "事件字段列表",
    modelName: "eventField",
    description: "事件字段",
    viewType: "grid",
    viewOptions: {},
    items: [
      { name: "fieldName", title: "字段名称", spanCols: 12 },
      { name: "fieldTitle", title: "字段标题", spanCols: 12 },
      { name: "fieldCategory", title: "字段类别", spanCols: 12 },
      { name: "fieldType", title: "字段类型", spanCols: 12 },
      { name: "fieldDescription", title: "字段描述", spanCols: 12, flex: 1 },
    ],
  },
  {
    name: "eventFieldFormView",
    title: "事件字段",
    modelName: "eventField",
    description: "事件字段",
    viewType: "form",
    density: "medium",
    items: [
      { name: "fieldName", title: "字段名称", spanCols: 12 },
      { name: "fieldTitle", title: "字段标题", spanCols: 12 },
      { name: "fieldCategory", title: "字段类别", spanCols: 12 },
      { name: "fieldType", title: "字段类型", spanCols: 12 },
      {
        name: "fieldAsInput",
        title: "字段作为输入",
        widget: "switch",
        spanCols: 12,
      },
      {
        name: "fieldDescription",
        title: "字段描述",
        spanCols: 12,
        flex: 1,
        widget: "textfield",
        widgetOptions: { multiline: true },
      },
    ],
  },
  {
    name: "actionGridView",
    title: "动作列表",
    modelName: "action",
    description: "动作",
    viewType: "grid",
    viewOptions: {},
    items: [
      { name: "name", title: "动作名称", spanCols: 12 },
      { name: "title", title: "动作标题", spanCols: 12 },
      {
        name: "fields",
        title: "动作字段",
        widget: "reference",
        width: 120,
        spanCols: 12,
      },
      { name: "description", title: "动作说明", spanCols: 12, flex: 1 },
    ],
  },
  {
    name: "actionFormView",
    title: "动作",
    modelName: "action",
    description: "动作",
    viewType: "form",
    density: "medium",
    items: [
      { name: "name", title: "动作名称", spanCols: 12 },
      { name: "title", title: "动作标题", spanCols: 12 },
      {
        name: "description",
        title: "动作说明",
        spanCols: 12,
        flex: 1,
        widget: "textfield",
        widgetOptions: { multiline: true },
      },
      { name: "fields", title: "动作字段", spanCols: 12 },
    ],
  },
  {
    name: "actionFieldGridView",
    title: "动作字段列表",
    modelName: "actionField",
    description: "动作字段",
    viewType: "grid",
    viewOptions: {},
    items: [
      { name: "fieldName", title: "字段名称", spanCols: 12 },
      { name: "fieldTitle", title: "字段标题", spanCols: 12 },
      { name: "fieldCategory", title: "字段类别", spanCols: 12 },
      { name: "fieldType", title: "字段类型", spanCols: 12 },
      { name: "fieldDescription", title: "字段描述", spanCols: 12, flex: 1 },
    ],
  },
  {
    name: "actionFieldFormView",
    title: "动作字段",
    modelName: "actionField",
    description: "动作字段",
    viewType: "form",
    density: "medium",
    items: [
      { name: "fieldName", title: "字段名称", spanCols: 12 },
      { name: "fieldTitle", title: "字段标题", spanCols: 12 },
      { name: "fieldCategory", title: "字段类别", spanCols: 12 },
      { name: "fieldType", title: "字段类型", spanCols: 12 },
      {
        name: "fieldDescription",
        title: "字段描述",
        spanCols: 12,
        flex: 1,
        widget: "textfield",
        widgetOptions: { multiline: true },
      },
    ],
  },
  {
    name: "productHomeView",
    title: "产品配置",
    modelName: "product",
    description: "产品",
    viewType: "mastail",
    viewOptions: {},
    items: [
      {
        name: "header",
        title: "产品标题",
        spanCols: 12,
        widget: "info",
        fields: [
          { name: "name", title: "产品名称", width: 200 },
          // { name: 'description', title: '产品说明', flex: 1 },
        ],
      },
      {
        name: "navigator",
        title: "基础配置",
        spanCols: -150,
        widget: "navigator",
        widgetOptions: {
          sx: {
            width: "auto",
          },
        },
        fields: [
          {
            name: "name",
            title: "基础配置",
            spanCols: 12,
            fields: [
              {
                name: "name",
                title: "产品信息",
                width: 200,
                widget: "action",
                icon: "material-icon-theme:redux-action",
                widgetOptions: {
                  actionType: "view",
                  payload: { modelName: "product", viewType: "form" },
                },
              },
              {
                name: "name",
                title: "通用配置",
                width: 200,
                widget: "action",
                icon: "material-icon-theme:settings",
                widgetOptions: {
                  actionType: "reference-view",
                  payload: {
                    fromFieldName: "settings",
                    toModelName: "productSetting",
                    viewType: "form",
                  },
                },
              },
            ],
          },
          {
            name: "event_action",
            title: "事件&动作",
            fields: [
              {
                name: "events",
                title: "事件配置",
                spanCols: 12,
                icon: "material-icon-theme:folder-git",
                widget: "action",
                widgetOptions: {
                  actionType: "reference-view",
                  payload: {
                    fromFieldName: "events",
                    toModelName: "event",
                    viewType: "grid",
                  },
                },
              },
              {
                name: "actions",
                title: "动作配置",
                spanCols: 12,
                icon: "material-icon-theme:folder-routes",
                widget: "action",
                widgetOptions: {
                  actionType: "reference-view",
                  payload: {
                    fromFieldName: "actions",
                    toModelName: "action",
                    viewType: "grid",
                  },
                },
              },
            ],
          },
          {
            name: "scenes",
            title: "场景",
            fields: [
              {
                name: "scenes",
                title: "场景配置",
                spanCols: 12,
                icon: "material-icon-theme:webpack",
                widget: "action",
                widgetOptions: {
                  actionType: "view",
                  payload: {
                    // fromFieldName: "rootScene",
                    // toModelName: "scene",
                    // viewType: "mastail",
                    // viewName: "sceneMastailView",
                    modelName: "scene",
                    viewType: "mastail",
                    viewName: "sceneMastailView",
                  },
                },
              },
              // {
              //   name: 'sceneGraph',
              //   title: '场景视图',
              //   spanCols: 12,
              //   icon: 'material-icon-theme:webpack',
              //   widget: 'action',
              //   widgetOptions: {
              //     actionType: 'comp',
              //     payload: { comp: SceneDesignHomeView },
              //   },
              // },
            ],
          },
          {
            name: "terminals",
            title: "终端",
            fields: [
              {
                name: "events",
                title: "连接配置",
                spanCols: 12,
                icon: "material-icon-theme:folder-connection",
                widget: "action",
                widgetOptions: {
                  actionType: "view",
                  payload: { modelName: "product", viewType: "grid" },
                },
              },
            ],
          },
          {
            name: "llm",
            title: "AI",
            fields: [
              {
                name: "events",
                title: "工具模型",
                spanCols: 12,
                icon: "material-icon-theme:robots",
                widget: "action",
                widgetOptions: {
                  actionType: "view",
                  payload: { modelName: "product", viewType: "grid" },
                },
              },
            ],
          },
        ],
      },
      {
        name: "detail",
        title: "详情视图",
        spanCols: 11,
        flex: 1,
        widget: "container",
        widgetOptions: {
          sx: {
            flexGrow: 1,
          },
        },
      },
    ],
  },
  {
    name: "llmSettingHomeView",
    title: "AI设置",
    modelName: "languageModelProvider",
    description: "AI设置",
    viewType: "mastail",
    viewOptions: {},
    items: [
      {
        name: "provider",
        title: "模型供应商配置",
        spanCols: 3,
        widget: "list",
        widgetOptions: {
          titleFieldName: "name",
          subtitleFieldName: "description",
          iconFieldName: "image",
        },
      },
      {
        name: "panel",
        title: "面板",
        spanCols: 9,
        fields: [
          {
            name: "mainTab",
            title: "标签",
            spanCols: 12,
            widget: "tab",
            fields: [
              {
                name: "general",
                title: "供应商信息",
                width: 200,
                widget: "action",
                icon: "material-icon-theme:settings",
                widgetOptions: {
                  actionType: "view",
                  payload: {
                    modelName: "languageModelProvider",
                    viewType: "form",
                  },
                },
              },
            ],
          },
        ],
      },
    ],
  },
  {
    name: "productGridView",
    title: "产品列表",
    modelName: "product",
    description: "产品",
    viewType: "grid",
    viewOptions: { mode: "grid" },
    items: [
      { name: "name", title: "产品名称", width: 200 },
      {
        name: "category",
        title: "产品分类",
        width: 100,
        widget: "select",
        widgetOptions: { color: "error" },
      },
      {
        name: "image",
        title: "产品图片",
        widget: "image",
        widgetOptions: { height: 40, width: 60, showInfo: false },
        width: 100,
      },
      { name: "description", title: "产品说明", flex: 1 },
      {
        name: "events",
        title: "事件",
        widget: "reference",
      },
      {
        name: "actions",
        title: "动作",
        widget: "reference",
      },
      {
        name: "settings",
        title: "参数",
        widget: "reference",
      },
      {
        name: "action",
        title: "配置",
        widget: "action",
        icon: "material-icon-theme:settings",
        widgetOptions: {
          actionType: "view",
          payload: { modelName: "product", viewType: "mastail" },
        },
        width: 100,
      },
    ],
    hilites: [
      // {when:'category === "0"', color:'#FFE9D5'},
      // {when:'category === "1"', color: '#f0f8fe'},
    ],
  },
  {
    name: "productFormView",
    title: "产品",
    modelName: "product",
    description: "产品",
    viewType: "form",
    density: "medium",
    items: [
      { name: "name", title: "产品名称", spanCols: 12 },
      {
        name: "category",
        title: "产品分类",
        widget: "select",
        widgetOptions: { color: "error" },
        spanCols: 12,
      },
      { name: "image", title: "产品图片", widget: "image", spanCols: 12 },
      {
        name: "description",
        title: "产品说明",
        widget: "textfield",
        widgetOptions: { multiline: true },
        spanCols: 12,
        flex: 1,
        hiddenWhen: 'category === "0"',
      },
      // {
      //   name: 'events',
      //   title: '事件',
      //   widget: 'references',
      //   spanCols: 12
      // },
      // {
      //   name: 'actions',
      //   title: '动作',
      //   widget: 'references',
      //   spanCols: 12
      // },
    ],
  },
  {
    name: "mainDashboardView",
    title: "主视图",
    modelName: "product",
    description: "系统主视图",
    viewType: "shell",
    viewOptions: {},
    items: [
      {
        name: "header",
        title: "智能物联网平台",
        widget: "logo",
        widgetOptions: {
          logoUrl: "https://lucide.dev/logo.light.svg",
          icon: "mdi:apple-keyboard-command",
          title: "智能物联网平台",
          logoWidth: 30,
          logoHeight: 30,
        },
        spanCols: 12,
        flex: 1,
      },
      {
        name: "navbar",
        title: "导航栏",
        widget: "navigator",
        widgetOptions: {
          padding: 2,
          spacing: 2,
        },
        spanCols: 12,
        flex: 1,
        fields: [
          {
            name: "overview-root",
            title: "概览",
            fields: [
              {
                name: "product-overview",
                title: "产品概览",
                icon: "material-icon-theme:redux-action",
                widget: "action",
                widgetOptions: {
                  actionType: "route",
                  payload: { path: "/dashboard/" },
                },
              },
              {
                name: "data-overview",
                title: "数据概览",
                icon: "material-icon-theme:database",
                widget: "action",
                widgetOptions: {
                  actionType: "route",
                  payload: { path: "/dashboard/data-overview" },
                },
              },
            ],
          },
          {
            name: "product-root",
            title: "产品",
            fields: [
              {
                name: "product",
                title: "产品管理",
                icon: "material-icon-theme:pdm",
                fields: [
                  {
                    name: "product-grid",
                    title: "产品列表",
                    widget: "action",
                    widgetOptions: {
                      actionType: "view",
                      payload: { modelName: "product", viewType: "grid" },
                    },
                  },
                ],
              },
            ],
          },
          {
            name: "resources-root",
            title: "资源",
            fields: [
              {
                name: "repositoris",
                title: "知识库",
                icon: "material-icon-theme:cbx",
                fields: [
                  {
                    name: "repositoris-grid",
                    title: "知识库列表",
                    widget: "action",
                    widgetOptions: {
                      actionType: "view",
                      payload: { modelName: "repositoris", viewType: "grid" },
                    },
                  },
                  {
                    name: "repositoris-new",
                    title: "创建知识库",
                    widget: "action",
                    widgetOptions: {
                      actionType: "view",
                      payload: { modelName: "repositoris", viewType: "form" },
                    },
                  },
                ],
              },
              {
                name: "resources",
                title: "资源库",
                icon: "material-icon-theme:taskfile",
                fields: [
                  {
                    name: "resource-grid",
                    title: "资源库列表",
                    widget: "action",
                    widgetOptions: {
                      actionType: "view",
                      payload: { modelName: "resource", viewType: "grid" },
                    },
                  },
                  {
                    name: "resource-new",
                    title: "创建资源",
                    widget: "action",
                    widgetOptions: {
                      actionType: "view",
                      payload: { modelName: "resource", viewType: "form" },
                    },
                  },
                ],
              },
            ],
          },
          {
            name: "ai-root",
            title: "AI设置",
            fields: [
              {
                name: "llmSetting",
                title: "大模型设置",
                spanCols: 12,
                icon: "material-icon-theme:robots",
                widget: "action",
                widgetOptions: {
                  actionType: "view",
                  payload: {
                    modelName: "languageModelProvider",
                    viewType: "mastail",
                  },
                },
              },
              {
                name: "mcpSetting",
                title: "MCP 设置",
                spanCols: 12,
                icon: "material-icon-theme:robots",
                widget: "action",
                widgetOptions: {
                  actionType: "view",
                  payload: {
                    modelName: "mcpService",
                    viewType: "grid",
                  },
                },
              },
            ],
          },
        ],
      },
      {
        name: "main",
        title: "主内容区",
        widget: "container",
        widgetOptions: {
          padding: 2,
          spacing: 2,
          sx: {
            flexGrow: 1,
            overflow: "auto",
            height: "calc(100vh - 128px)",
          },
        },
        spanCols: 12,
        flex: 1,
      },
    ],
  },
];

export const rootMenuItem: IEntityViewMenuItem = {
  name: "root",
  title: "根菜单",
  items: [
    {
      name: "overview-root",
      title: "概览",
      items: [
        {
          name: "product-overview",
          title: "产品概览",
          icon: "material-icon-theme:redux-action",
          action: { type: "route", payload: { path: "/dashboard/" } },
        },
        {
          name: "data-overview",
          title: "数据概览",
          icon: "material-icon-theme:folder-database-open",
          action: { type: "route", payload: { path: "/dashboard/ecommerce/" } },
        },
      ],
    },
    {
      name: "product-root",
      title: "产品",
      items: [
        {
          name: "product",
          title: "产品管理",
          icon: "material-icon-theme:pdm",
          items: [
            {
              name: "product-grid",
              title: "产品列表",
              action: {
                type: "view",
                payload: { modelName: "product", viewType: "grid" },
              },
            },
            {
              name: "product-new",
              title: "创建产品",
              action: {
                type: "view",
                payload: { modelName: "product", viewType: "form" },
              },
            },
            { name: "product-demo", title: "示例产品" },
          ],
        },
      ],
    },
    {
      name: "resources-root",
      title: "资源",
      items: [
        {
          name: "repositoris",
          title: "知识库",
          icon: "material-icon-theme:cbx",
          items: [
            {
              name: "repositoris-grid",
              title: "知识库列表",
              action: {
                type: "view",
                payload: { modelName: "repositoris", viewType: "grid" },
              },
            },
            {
              name: "repositoris-new",
              title: "创建知识库",
              action: {
                type: "view",
                payload: { modelName: "repositoris", viewType: "form" },
              },
            },
          ],
        },
        {
          name: "resources",
          title: "资源库",
          icon: "material-icon-theme:taskfile",
          items: [
            {
              name: "resource-grid",
              title: "资源库列表",
              action: {
                type: "view",
                payload: { modelName: "resource", viewType: "grid" },
              },
            },
            {
              name: "resource-new",
              title: "创建资源",
              action: {
                type: "view",
                payload: { modelName: "resource", viewType: "form" },
              },
            },
          ],
        },
      ],
    },
    {
      name: "data-root",
      title: "数据",
      items: [
        {
          name: "data-analysis",
          title: "数据分析",
          icon: "material-icon-theme:regedit",
          action: { type: "route", payload: { path: "/data/analysis" } },
        },
        {
          name: "terminal-analysis",
          title: "终端分析",
          icon: "material-icon-theme:cline",
          action: { type: "route", payload: { path: "/terminal/analysis" } },
        },
        {
          name: "user-analysis",
          title: "用户分析",
          icon: "material-icon-theme:java",
          action: { type: "route", payload: { path: "/user/analysis" } },
        },
      ],
    },
    {
      name: "settings-root",
      title: "设置",
      items: [
        {
          name: "settings",
          title: "大模型设置",
          icon: "material-icon-theme:robots",
          action: {
            type: "view",
            payload: {
              modelName: "languageModelProvider",
              viewType: "mastail",
            },
          },
        },
        {
          name: "settings",
          title: "MCP设置",
          icon: "material-icon-theme:robot",
          action: {
            type: "view",
            payload: { modelName: "mcpService", viewType: "grid" },
          },
        },
      ],
    },
  ],
};
