package com.scene.mesh.model.product;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.scene.mesh.model.scene.WhenThen;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class OriginalProduct {
    private String id;
    private String modelName;
    private Values values;
    private Boolean isDeleted;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Values {
        private String name;
        private Image image;
        private String category;
        private List<Setting> settings;
        private List<Scene> rootScene;
        private String description;
        private List<Action> actions;
        private List<Event> events;

        // 处理settings字段的混合数据类型
        @JsonSetter("settings")
        public void setSettingsFromObject(Object settingsObj) {
            if (settingsObj instanceof String) {
                // 如果是字符串（通常是空字符串），设置为空列表
                this.settings = new ArrayList<>();
            } else if (settingsObj instanceof List<?> rawList) {
                // 如果是列表，使用ObjectMapper转换每个元素
                try {
                    ObjectMapper mapper = new ObjectMapper();
                    this.settings = new ArrayList<>();
                    for (Object item : rawList) {
                        Setting setting = mapper.convertValue(item, Setting.class);
                        this.settings.add(setting);
                    }
                } catch (Exception e) {
                    this.settings = new ArrayList<>();
                }
            } else {
                this.settings = new ArrayList<>();
            }
        }

        // 处理rootScene字段的混合数据类型
        @JsonSetter("rootScene")
        public void setRootSceneFromObject(Object rootSceneObj) {
            if (rootSceneObj instanceof String) {
                // 如果是字符串（通常是空字符串），设置为空列表
                this.rootScene = new ArrayList<>();
            } else if (rootSceneObj instanceof List<?> rawList) {
                // 如果是列表，使用ObjectMapper转换每个元素
                try {
                    ObjectMapper mapper = new ObjectMapper();
                    this.rootScene = new ArrayList<>();
                    for (Object item : rawList) {
                        Scene scene = mapper.convertValue(item, Scene.class);
                        this.rootScene.add(scene);
                    }
                } catch (Exception e) {
                    this.rootScene = new ArrayList<>();
                }
            } else {
                this.rootScene = new ArrayList<>();
            }
        }

        // 处理actions字段的混合数据类型
        @JsonSetter("actions")
        public void setActionsFromObject(Object actionsObj) {
            if (actionsObj instanceof String) {
                // 如果是字符串（通常是空字符串），设置为空列表
                this.actions = new ArrayList<>();
            } else if (actionsObj instanceof List<?> rawList) {
                // 如果是列表，使用ObjectMapper转换每个元素
                try {
                    ObjectMapper mapper = new ObjectMapper();
                    this.actions = new ArrayList<>();
                    for (Object item : rawList) {
                        Action action = mapper.convertValue(item, Action.class);
                        this.actions.add(action);
                    }
                } catch (Exception e) {
                    this.actions = new ArrayList<>();
                }
            } else {
                this.actions = new ArrayList<>();
            }
        }

        // 处理events字段的混合数据类型
        @JsonSetter("events")
        public void setEventsFromObject(Object eventsObj) {
            if (eventsObj instanceof String) {
                // 如果是字符串（通常是空字符串），设置为空列表
                this.events = new ArrayList<>();
            } else if (eventsObj instanceof List<?> rawList) {
                // 如果是列表，使用ObjectMapper转换每个元素
                try {
                    ObjectMapper mapper = new ObjectMapper();
                    this.events = new ArrayList<>();
                    for (Object item : rawList) {
                        Event event = mapper.convertValue(item, Event.class);
                        this.events.add(event);
                    }
                } catch (Exception e) {
                    this.events = new ArrayList<>();
                }
            } else {
                this.events = new ArrayList<>();
            }
        }
    }

    @Data
    public static class Image {
        private String fileName;
        private String filePath;
        private Integer fileSize;
        private String fileType;
    }

    @Data
    public static class Setting {
        private String id;
        private String modelName;
        private SettingValues values;
        private Boolean isDeleted;
    }

    @Data
    public static class SettingValues {
        private String secret;
        private Integer mqttPort;
        private Boolean mqttEnabled;
        private Integer webSocketPort;
        private Boolean webSocketEnabled;
    }

    @Data
    public static class Scene {
        private String id;
        private String modelName;
        private SceneValues values;
        private Boolean isDeleted;
    }

    @Data
    public static class SceneValues {
        private String flow;
        private String name;
        private String input;
        private Rules rules;
        private Boolean enable;
        private String prompt;
        private List<WhenThen> flowData;
        private Integer timeWindow;
        private String description;
        private String promptInherited;
        private String flowDataPublishTime;
        private List<Scene> children;

        @JsonSetter("flowData")
        public void setFlowDataFromString(Object flowDataObj) {
            if (flowDataObj instanceof String flowDataStr) {
                if (flowDataStr == null || flowDataStr.trim().isEmpty()) {
                    this.flowData = new ArrayList<>();
                    return;
                }

                try {
                    ObjectMapper mapper = new ObjectMapper();
                    this.flowData = mapper.readValue(flowDataStr, new TypeReference<>() {});
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            } else if (flowDataObj instanceof List<?> rawList) {
                // 如果是列表，使用ObjectMapper转换每个元素
                try {
                    ObjectMapper mapper = new ObjectMapper();
                    this.flowData = new ArrayList<>();
                    for (Object item : rawList) {
                        WhenThen whenThen = mapper.convertValue(item, WhenThen.class);
                        this.flowData.add(whenThen);
                    }
                } catch (Exception e) {
                    this.flowData = new ArrayList<>();
                }
            } else {
                this.flowData = new ArrayList<>();
            }
        }

        // 处理children字段的混合数据类型
        @JsonSetter("children")
        public void setChildrenFromObject(Object childrenObj) {
            if (childrenObj instanceof String) {
                // 如果是字符串（通常是空字符串），设置为空列表
                this.children = new ArrayList<>();
            } else if (childrenObj instanceof List<?> rawList) {
                // 如果是列表，使用ObjectMapper转换每个元素
                try {
                    ObjectMapper mapper = new ObjectMapper();
                    this.children = new ArrayList<>();
                    for (Object item : rawList) {
                        Scene scene = mapper.convertValue(item, Scene.class);
                        this.children.add(scene);
                    }
                } catch (Exception e) {
                    this.children = new ArrayList<>();
                }
            } else {
                this.children = new ArrayList<>();
            }
        }
    }

    @Data
    public static class Rules {
        private String id;
        private List<Object> rules;
        private String combinator;

        // 处理rules字段的混合数据类型
        @JsonSetter("rules")
        public void setRulesFromObject(Object rulesObj) {
            if (rulesObj instanceof String) {
                // 如果是字符串（通常是空字符串），设置为空列表
                this.rules = new ArrayList<>();
            } else if (rulesObj instanceof List) {
                // 如果是列表，直接设置（因为是List<Object>）
                this.rules = (List<Object>) rulesObj;
            } else {
                this.rules = new ArrayList<>();
            }
        }
    }

    @Data
    public static class Action {
        private String id;
        private String modelName;
        private ActionValues values;

        @JsonProperty("isDeleted")
        private Boolean isDeleted;
    }

    @Data
    public static class ActionValues {
        private String name;
        private String title;
        private String description;
        private List<ActionField> fields;

        // 处理fields字段的混合数据类型
        @JsonSetter("fields")
        public void setFieldsFromObject(Object fieldsObj) {
            if (fieldsObj instanceof String) {
                // 如果是字符串（通常是空字符串），设置为空列表
                this.fields = new ArrayList<>();
            } else if (fieldsObj instanceof List<?> rawList) {
                // 如果是列表，使用ObjectMapper转换每个元素
                try {
                    ObjectMapper mapper = new ObjectMapper();
                    this.fields = new ArrayList<>();
                    for (Object item : rawList) {
                        ActionField field = mapper.convertValue(item, ActionField.class);
                        this.fields.add(field);
                    }
                } catch (Exception e) {
                    this.fields = new ArrayList<>();
                }
            } else {
                this.fields = new ArrayList<>();
            }
        }
    }

    @Data
    public static class ActionField {
        private String id;
        private String modelName;
        private ActionFieldValues values;

        @JsonProperty("isDeleted")
        private Boolean isDeleted;
    }

    @Data
    public static class ActionFieldValues {
        private String fieldName;
        private String fieldType;
        private String fieldTitle;
        private String fieldCategory;
        private String fieldCalculateType;
        private String fieldCalculateSource;
        private String fieldDescription;
        private Boolean fieldAsInput;
    }

    @Data
    public static class Event {
        private String id;
        private String modelName;
        private EventValues values;

        @JsonProperty("isDeleted")
        private Boolean isDeleted;
    }

    @Data
    public static class EventValues {
        private String name;
        private String title;
        private String description;
        private List<EventField> fields;

        // 处理fields字段的混合数据类型
        @JsonSetter("fields")
        public void setFieldsFromObject(Object fieldsObj) {
            if (fieldsObj instanceof String) {
                // 如果是字符串（通常是空字符串），设置为空列表
                this.fields = new ArrayList<>();
            } else if (fieldsObj instanceof List<?> rawList) {
                // 如果是列表，使用ObjectMapper转换每个元素
                try {
                    ObjectMapper mapper = new ObjectMapper();
                    this.fields = new ArrayList<>();
                    for (Object item : rawList) {
                        EventField field = mapper.convertValue(item, EventField.class);
                        this.fields.add(field);
                    }
                } catch (Exception e) {
                    this.fields = new ArrayList<>();
                }
            } else {
                this.fields = new ArrayList<>();
            }
        }
    }

    @Data
    public static class EventField {
        private String id;
        private String modelName;
        private EventFieldValues values;

        @JsonProperty("isDeleted")
        private Boolean isDeleted;
    }

    @Data
    public static class EventFieldValues {
        private String fieldName;
        private String fieldType;
        private String fieldTitle;
        private Boolean fieldAsInput;
        private String fieldCategory;
        private String fieldCalculateType;
        private String fieldCalculateSource;
        private String fieldDescription;
    }
}