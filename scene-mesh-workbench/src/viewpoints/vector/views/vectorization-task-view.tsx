"use client";

import React, { useState } from 'react';
import {
    Box,
    Text,
    Group,
    Badge,
    Button,
} from '@mantine/core';
import { Icon } from '@iconify/react';
import { toDataSourceHook, useEntityEngine, useMasterDetailViewContainer } from '@scenemesh/entity-engine';
import { ContentChunkComp } from '../componemt/content-chunk-comp';

export function VectorizationTaskView() {
    const { currentAction, parentContext } = useMasterDetailViewContainer();
    const knowledgeBaseId = parentContext?.id;
    const knowledgeItem = currentAction?.contextObject;
    const knowledgeItemId = knowledgeItem?.id;
    const vectorizationStrategyId = knowledgeItem?.values?.vectorizationStrategy;
    const contentId = knowledgeItem?.values?.content;

    if (!knowledgeBaseId || !knowledgeItemId || !vectorizationStrategyId || !contentId) {
        throw new Error('knowledgeBaseId or knowledgeItemId or vectorizationStrategyId or contentId is not found');
    }

    const engine = useEntityEngine();
    const dataSource = engine.datasourceFactory.getDataSource();
    const dataSourceHooks = toDataSourceHook(dataSource);

    const [vectorizationStatus, setVectorizationStatus] = useState<'init' | 'successful' | 'unsuccessful'>('init');
    const [vectorizationMessage, setVectorizationMessage] = useState<any>(null);
    const [deleteStatus, setDeleteStatus] = useState<true | false>(false);
    const [deleteMessage, setDeleteMessage] = useState<any>(null);
    const [isLoading, setIsLoading] = useState(false); // 添加loading状态
    const [chunksRefresh, setChunksRefresh] = useState(Date.now());

    const {
        data: knowledgeBase,
        loading: knowledgeBaseLoading,
        error: knowledgeBaseError,
        isFetching: knowledgeBaseIsFetching
    } = dataSourceHooks.useFindOneWithReferences({
        modelName: "knowledgeBase",
        id: knowledgeBaseId
    });


    const { data: content,
        loading: contentLoading,
        error: contentError,
        isFetching: contentIsFetching } = dataSourceHooks.useFindOneWithReferences({
            modelName: "content",
            id: contentId,
        }
        );

    const {
        data: vectorizationStrategy,
        loading: vectorizationStrategyLoading,
        error: vectorizationStrategyError,
        isFetching: vectorizationStrategyIsFetching
    } = dataSourceHooks.useFindOneWithReferences({
        modelName: "vectorizationStrategy",
        id: vectorizationStrategyId
    }
    );

    if (knowledgeBaseIsFetching || knowledgeBaseLoading || !knowledgeBase) {
        return <Box>Loading...</Box>;
    }

    if (knowledgeBaseError) {
        throw new Error(knowledgeBaseError.message);
    }

    if (contentIsFetching || contentLoading || !content) {
        return <Box>Loading...</Box>;
    }

    if (contentError) {
        throw new Error(contentError.message);
    }

    if (vectorizationStrategyIsFetching || vectorizationStrategyLoading || !vectorizationStrategy) {
        return <Box>Loading...</Box>;
    }

    if (vectorizationStrategyError) {
        throw new Error(vectorizationStrategyError.message);
    }

    console.log('==========>vectorizationStrategy: ', vectorizationStrategy);

    const contentUrl = process.env.NEXT_PUBLIC_API_BASE_URL + "/uploads/" + content.values?.file.filePath;
    const providerName = knowledgeBase.values?.provider.values?.name;
    const modelName = knowledgeBase.values?.embeddingsModel.values?.name;

    const taskData = {
        knowledgeBase_id: knowledgeBaseId,
        knowledgeItem_id: knowledgeItemId,
        content_url: contentUrl,
        options: {
            providerName: providerName,
            modelName: modelName,
            chunkSize: vectorizationStrategy.values?.chunkSize,
            minChunkSizeChars: vectorizationStrategy.values?.minChunkSizeChars,
            minChunkLengthToEmbed: vectorizationStrategy.values?.minChunkLengthToEmbed,
            maxNumChunks: vectorizationStrategy.values?.maxNumChunks,
        }
    }

    console.log('==========>taskData: ', taskData);


    const onSubmit = async (taskData: any) => {
        try {
            // 设置loading状态
            setIsLoading(true);

            // 调用服务端向量化API
            const response = await fetch(`/api/vectorization`, {
                method: 'POST',
                body: JSON.stringify(taskData),
                headers: {
                    'Content-Type': 'application/json',
                },
            });

            if (response.ok) {
                const result = await response.json();

                // 处理返回的数据
                const { taskStatus, statusMessage } = result;
                // 根据任务状态更新UI
                setVectorizationStatus(taskStatus);
                setVectorizationMessage(statusMessage);

            } else {
                // 处理HTTP错误
                const errorData = await response.json().catch(() => ({}));
                setVectorizationStatus('unsuccessful');
                setVectorizationMessage(errorData);
            }

        } catch (error) {
            // 处理网络错误或其他异常
            setVectorizationStatus('unsuccessful');
            setVectorizationMessage(error instanceof Error ? error.message : 'Unknown error');

        } finally {
            // 无论成功还是失败，都要关闭loading状态
            setIsLoading(false);
            setChunksRefresh(Date.now());
        }
    }

    const onDelete = async (providerName: string, modelName: string, knowledgeBaseId: string, knowledgeItemId: string) => {
        try {
            setIsLoading(true);
            const queryParams = new URLSearchParams({
                providerName,
                modelName,
                knowledgeBaseId,
                knowledgeItemId
            });
            const response = await fetch(`/api/vectorization?${queryParams}`, {
                method: 'DELETE'
            });
            const result = await response.json();
            const status = Object.keys(result)[0];
            const message = Object.values(result)[0];
            setDeleteStatus(status as any);
            setDeleteMessage(message);
        } catch (error) {
            setDeleteStatus(false);
            setDeleteMessage(error instanceof Error ? error.message : 'Unknown error');
        } finally {
            setIsLoading(false);
            setChunksRefresh(Date.now());
        }
    }

    // 获取状态颜色
    const getStatusColor = (status: string) => {
        switch (status) {
            case 'successful':
                return 'green';
            case 'unsuccessful':
                return 'red';
            default:
                return 'gray';
        }
    };

    // 获取状态文本
    const getStatusText = (status: string) => {
        switch (status) {
            case 'successful':
                return '已完成';
            case 'unsuccessful':
                return '处理失败';
            default:
                return '未知';
        }
    };

    // 获取状态图标
    const getStatusIcon = (status: string) => {
        switch (status) {
            case 'successful':
                return 'mdi:check-circle';
            case 'unsuccessful':
                return 'mdi:close-circle';
            default:
                return 'mdi:help-circle';
        }
    };

    return (
        <Box p="md" style={{ maxWidth: '1200px', margin: '0 auto', position: 'relative' }}>

            {/* 全页面Loading蒙层 */}
            {isLoading && (
                <Box
                    style={{
                        position: 'fixed',
                        top: 0,
                        left: 0,
                        right: 0,
                        bottom: 0,
                        backgroundColor: 'rgba(255, 255, 255, 0.9)',
                        backdropFilter: 'blur(3px)',
                        zIndex: 9999,
                        display: 'flex',
                        alignItems: 'center',
                        justifyContent: 'center',
                        flexDirection: 'column',
                        gap: '20px',
                    }}
                >
                    {/* Loading图标 */}
                    <Box
                        style={{
                            width: '48px',
                            height: '48px',
                            border: '4px solid var(--mantine-color-blue-3)',
                            borderTop: '4px solid var(--mantine-color-blue-6)',
                            borderRadius: '50%',
                            animation: 'spin 1s linear infinite',
                        }}
                    />

                    {/* Loading文字 */}
                    <Text size="xl" fw={600} c="blue">
                        正在处理向量化任务...
                    </Text>

                    {/* 副标题 */}
                    <Text size="md" c="dimmed" ta="center">
                        请稍候，这可能需要几分钟时间
                    </Text>
                </Box>
            )}
            <Group justify="space-between" mb="lg" gap="xs">
                <Group justify="left" mb="lg" gap="xs">
                    <Button
                        variant="filled"
                        color="green"
                        size="sm"
                        h={30}
                        onClick={() => onSubmit(taskData)}
                        loading={isLoading}
                        disabled={isLoading}
                    >
                        <Icon icon="mdi:arrow-right" />
                        <Text size="sm">执行</Text>
                    </Button>

                    {vectorizationStatus !== 'init' && (
                        <Badge
                            color={getStatusColor(vectorizationStatus)}
                            size="md"
                            variant="light"
                            h={30}
                            style={{ padding: '8px 12px' }}
                        >
                            <Group>
                                <Icon
                                    icon={getStatusIcon(vectorizationStatus)}
                                    style={{ marginRight: '6px', fontSize: '14px' }}
                                />
                                <Text size="sm">{getStatusText(vectorizationStatus)}</Text>
                                <Text size="sm">{vectorizationMessage}</Text>
                            </Group>
                        </Badge>
                    )}
                </Group>

                <Group justify="left" mb="lg" gap="xs">

                    {deleteStatus !== false && (
                        <Badge
                            color={getStatusColor(deleteStatus?'successful':'unsuccessful')}
                            size="md"
                            variant="light"
                            h={30}
                            style={{ padding: '8px 12px' }}
                        >
                            <Group>
                                <Icon icon={getStatusIcon(deleteStatus?'successful':'unsuccessful')} style={{ marginRight: '6px', fontSize: '14px' }} />
                                <Text size="sm">{getStatusText(deleteStatus?'successful':'unsuccessful')}</Text>
                                <Text size="sm">{deleteMessage}</Text>
                            </Group>
                        </Badge>
                    )}
                    <Button
                        variant="filled"
                        color="red"
                        size="sm"
                        h={30}
                        onClick={() => onDelete(providerName, modelName, knowledgeBaseId, knowledgeItemId)}
                        loading={isLoading}
                        disabled={isLoading}
                    >
                        <Icon icon="mdi:delete" />
                        <Text size="sm">删除</Text>
                    </Button>
                </Group>
            </Group>
            <ContentChunkComp
                providerName={providerName}
                modelName={modelName}
                knowledgeBaseId={knowledgeBaseId}
                knowledgeItemId={knowledgeItemId}
                chunksRefresh={chunksRefresh}
            />
        </Box>
    );
}