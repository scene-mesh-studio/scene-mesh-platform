import {
    Box,
    Card,
    Text,
    Group,
    Badge,
} from '@mantine/core';
import { Icon } from '@iconify/react';
import { useEffect, useState } from 'react';

interface ContentChunkCompProps {
    providerName: string;
    modelName: string;
    knowledgeBaseId: string;
    knowledgeItemId: string;
    chunksRefresh: number;
}

export function ContentChunkComp(props: ContentChunkCompProps) {
    const { providerName, modelName, knowledgeBaseId, knowledgeItemId ,chunksRefresh } = props;
    const [textChunks, setTextChunks] = useState<any>([]); // 分块数据

    useEffect(() => {
        const fetchTextChunks = async () => {

            const queryParams = new URLSearchParams({
                knowledgeBaseId,
                knowledgeItemId,
                providerName,
                modelName
            });

            const response = await fetch(`/api/vectorization?${queryParams}`, {
                method: 'GET'
            });

            if (response.ok) {
                const result = await response.json();
                console.log('==========>result: ', result);
                if (result.error) {
                    console.error('==========>error: ', result.error);
                    setTextChunks([]);
                } else {
                    setTextChunks(result);
                }
        
            } else {
                // 处理HTTP错误
                const errorData = await response.json().catch(() => ({}));
                console.error('==========>errorData: ', errorData);
                setTextChunks([]);
            }
            console.log('==========>textChunks: ', textChunks);
        }
        fetchTextChunks();
    },[providerName, modelName, knowledgeBaseId, knowledgeItemId, chunksRefresh, textChunks]);

    return (
    <Card shadow="sm" p="lg" radius="md">
        {/* 文本分块 */}
        <Box>
            <Group justify="space-between" mb="md">
                <Group gap="xs">
                    <Icon
                        icon="streamline-plump-color:transparent"
                        style={{ fontSize: '20px' }}
                    />
                    <Text size="sm" fw={500}>文本分块</Text>
                </Group>
                <Text size="sm" c="dimmed">共 {textChunks?.length || 0} 个分段</Text>
            </Group>

            <Box style={{
                display: 'grid',
                gridTemplateColumns: 'repeat(auto-fit, minmax(300px, 1fr))',
                gap: '16px'
            }}>
                {textChunks?.map((chunk: any, index: number) => (
                    <Card
                        key={index}
                        p="md"
                        radius="md"
                        withBorder
                        style={{
                            backgroundColor: 'var(--mantine-color-gray-0)',
                            position: 'relative'
                        }}
                    >
                        {/* 分段编号和状态 */}
                        <Group justify="space-between" mb="xs">
                            <Badge size="sm" variant="light" color="blue">
                                #{index + 1}:{chunk.id}
                            </Badge>
                            <Group gap="xs">
                                <Text size="xs" c="dimmed">
                                    {chunk.text.length}字
                                </Text>
                                <Icon
                                    icon="mdi:check-circle"
                                    color="green"
                                    style={{ fontSize: '16px' }}
                                />
                            </Group>
                        </Group>

                        {/* 文本内容 */}
                        <Text size="sm" lineClamp={4} style={{ lineHeight: '1.5' }}>
                            {chunk.text}
                        </Text>
                    </Card>
                ))}
            </Box>
        </Box>
    </Card>);
}