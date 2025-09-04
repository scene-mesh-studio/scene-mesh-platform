import {
    Card, Box, Group, Text, Badge, Select, Pagination, Loader
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
    const { providerName, modelName, knowledgeBaseId, knowledgeItemId, chunksRefresh } = props;
    const [textChunks, setTextChunks] = useState<any>([]); // 分块数据
    const [total, setTotal] = useState(0); // 总数
    const [currentPage, setCurrentPage] = useState(1); // 当前页
    const [pageSize, setPageSize] = useState(10); // 每页大小
    const [loading, setLoading] = useState(false); // 加载状态

    useEffect(() => {
        const fetchTextChunks = async () => {
            setLoading(true);
            try {
                const queryParams = new URLSearchParams({
                    knowledgeBaseId,
                    knowledgeItemId,
                    providerName,
                    modelName,
                    page: currentPage.toString(),
                    size: pageSize.toString()
                });

                const response = await fetch(`/api/vectorization?${queryParams}`, {
                    method: 'GET'
                });

                if (response.ok) {
                    const result = await response.json();
                    console.log('==========>result: ', result);
                    if (result.error) {
                        console.error('==========>error: ', result.error);
                    } else {
                        // 处理分页结果
                        setTextChunks(result.data);
                        setTotal(result.total || 0);
                    }
                } else {
                    const errorData = await response.json().catch(() => ({}));
                    console.error('==========>errorData: ', errorData);
                }
            } catch (error) {
                console.error('==========>fetch error: ', error);
            } finally {
                setLoading(false);
            }
        }
        fetchTextChunks();
    }, [providerName, modelName, knowledgeBaseId, knowledgeItemId, chunksRefresh, currentPage, pageSize]);

    // 处理页码变化
    const handlePageChange = (page: number) => {
        setCurrentPage(page);
    };

    // 处理每页大小变化
    const handlePageSizeChange = (value: string | null) => {
        if (value) {
            setPageSize(Number(value));
            setCurrentPage(1); // 重置到第一页
        }
    };

    // 计算当前页的起始索引
    const getStartIndex = () => (currentPage - 1) * pageSize;

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
                    <Group gap="md">
                        <Text size="sm" c="dimmed">共 {total} 个分段</Text>
                        {/* 每页大小选择器 */}
                        <Select
                            size="xs"
                            value={pageSize.toString()}
                            onChange={handlePageSizeChange}
                            data={[
                                { value: '5', label: '5条/页' },
                                { value: '10', label: '10条/页' },
                                { value: '20', label: '20条/页' },
                                { value: '50', label: '50条/页' }
                            ]}
                        />
                    </Group>
                </Group>

                {/* 加载状态 */}
                {loading && (
                    <Box ta="center" py="xl">
                        <Loader size="sm" />
                    </Box>
                )}

                {/* 分块内容 */}
                {!loading && (
                    <Box style={{
                        display: 'grid',
                        gridTemplateColumns: 'repeat(auto-fit, minmax(300px, 1fr))',
                        gap: '16px'
                    }}>
                        {textChunks?.map((chunk: any, index: number) => (
                            <Card
                                key={chunk.id || `${currentPage}-${index}`}
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
                                        #{getStartIndex() + index + 1}:{chunk.id}
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
                )}

                {/* 分页器 */}
                {total > pageSize && (
                    <Box mt="lg" ta="center">
                        <Pagination
                            total={Math.ceil(total / pageSize)}
                            value={currentPage}
                            onChange={handlePageChange}
                            size="sm"
                            withEdges
                            siblings={1}
                        />
                    </Box>
                )}

                {/* 空状态 */}
                {!loading && (!textChunks || textChunks.length === 0) && (
                    <Box ta="center" py="xl">
                        <Text c="dimmed">暂无分块数据</Text>
                    </Box>
                )}
            </Box>
        </Card>
    );
}