'use client';

import React, { useState, useEffect } from 'react';
import { Upload, Trash2, File } from 'lucide-react';
import {
    Box,
    Text,
    Group,
    Stack,
    Loader,
    Tooltip,
    ActionIcon,
    Button,
} from '@mantine/core';

import { type EntityWidgetProps } from "@scenemesh/entity-engine";

// 文件数据结构接口
interface FileData {
    fileName: string;
    filePath: string;
    fileSize: number;
    fileType: string;
}

// 扩展的组件属性接口
interface FileUploadCompProps extends EntityWidgetProps {
    onFileUpload?: (file: File) => Promise<FileData>;
    accept?: string | undefined;
    maxSize?: number | undefined;
    multiple?: boolean;
    showInfo?: boolean;
}

// 辅助函数：获取文件信息
const getFileInfo = (value: any): FileData | null => {
    if (!value) return null;

    // 如果是文件数据对象
    if (typeof value === 'object' && value.fileName && value.filePath) {
        return value as FileData;
    }

    return null;
};

// 辅助函数：格式化文件大小
const formatFileSize = (bytes: number): string => {
    if (bytes === 0) return '0 Bytes';
    const k = 1024;
    const sizes = ['Bytes', 'KB', 'MB', 'GB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
};

export default function FileUploadComp(props: FileUploadCompProps) {
    const { behavior, maintain } = props;
    const readonly = maintain?.readonly || false;

    if (behavior.mode === 'edit') {
        return <InnerFileUploadEdit {...props} readonly={readonly} />;
    } else {
        return <InnerFileUploadDisplay {...props} />;
    }
}

function InnerFileUploadDisplay(props: FileUploadCompProps) {
    const { value, showInfo } = props;
    const fileInfo = getFileInfo(value);

    if (!fileInfo) {
        return (
            <Box
                p="md"
                style={{
                    border: '1px dashed #ced4da',
                    borderRadius: '4px',
                    textAlign: 'start',
                }}
            >
                <Text size="sm" c="dimmed">
                    无文件
                </Text>
            </Box>
        );
    }

    return (
        <Stack gap="xs" align="start">
            <Group gap="sm" align="center">
                <File size={20} color="#6c757d" />
                <Box>
                    <Text size="sm" fw={500}>
                        {fileInfo.fileName}
                    </Text>
                    {showInfo && (
                        <Text size="xs" c="dimmed">
                            {formatFileSize(fileInfo.fileSize)} • {fileInfo.fileType}
                        </Text>
                    )}
                </Box>
            </Group>
        </Stack>
    );
}

function InnerFileUploadEdit(props: FileUploadCompProps & { readonly: boolean }) {
    const { value, fieldControl, readonly, onFileUpload, accept, maxSize, multiple, showInfo } = props;
    const [currentValue, setCurrentValue] = useState(value);
    const [uploadError, setUploadError] = useState<string | null>(null);
    const [uploading, setUploading] = useState(false);

    // 监听 value 变化，同步到 currentValue
    useEffect(() => {
        setCurrentValue(value);
    }, [value]);

    const fileInfo = getFileInfo(currentValue);

    // 处理文件上传
    const handleFileChange = async (file: File | null) => {
        if (!file) {
            return;
        }

        // 验证文件类型
        const acceptTypes = accept || '*/*';
        if (acceptTypes !== '*/*') {
            const acceptedTypes = acceptTypes.split(',').map((type) => type.trim());
            const isValidType = acceptedTypes.some((type) => {
                if (type.endsWith('/*')) return file.type.startsWith(type.replace('/*', '/'));
                return file.type === type || file.name.toLowerCase().endsWith(type.replace('*', ''));
            });

            if (!isValidType) {
                setUploadError(`请选择支持的文件类型: ${acceptTypes}`);
                return;
            }
        }

        // 验证文件大小
        const fileSizeLimit = maxSize || 10 * 1024 * 1024; // 默认10MB
        if (file.size > fileSizeLimit) {
            setUploadError(`文件大小不能超过 ${formatFileSize(fileSizeLimit)}`);
            return;
        }

        setUploadError(null);
        setUploading(true);

        try {
            if (onFileUpload) {
                // 使用外部提供的上传方法
                const fileData = await onFileUpload(file);
                fieldControl?.onChange?.(fileData);
                setCurrentValue(fileData);
            } else {
                // 回退到本地 base64 处理
                const reader = new FileReader();
                reader.onload = (e) => {
                    const result = e.target?.result as string;

                    // 创建文件数据对象
                    const fileData: FileData = {
                        fileName: file.name,
                        filePath: result, // 这里使用 base64 作为临时路径
                        fileSize: file.size,
                        fileType: file.type,
                    };

                    fieldControl?.onChange?.(fileData);
                    setCurrentValue(fileData);
                    setUploading(false);
                };
                reader.onerror = () => {
                    setUploadError('文件读取失败');
                    setUploading(false);
                };
                reader.readAsDataURL(file);
            }
        } catch (error) {
            console.error('文件上传失败:', error);
            setUploadError(error instanceof Error ? error.message : '文件上传失败');
            setUploading(false);
        }
    };

    // 清除文件
    const handleClear = () => {
        fieldControl?.onChange?.(null);
        setCurrentValue(null);
        setUploadError(null);
    };

    // 处理文件选择
    const handleFileSelect = () => {
        if (uploading || readonly) return;

        const input = document.createElement('input');
        input.type = 'file';
        input.accept = accept || '*/*';
        input.multiple = multiple || false;
        input.onchange = (e) => {
            const files = (e.target as HTMLInputElement).files;
            if (files && files.length > 0) {
                handleFileChange(files[0]); // 暂时只处理第一个文件
            }
        };
        input.click();
    };

    if (readonly) {
        return <InnerFileUploadDisplay {...props} />;
    }

    return (
        <Stack gap="xs">
            {fileInfo ? (
                // 有文件时显示文件信息
                <Stack gap="xs" align="start">
                    <Group gap="sm" align="center">
                        <File size={20} color="#6c757d" />
                        <Box>
                            <Text size="sm" fw={500}>
                                {fileInfo.fileName}
                            </Text>
                            {showInfo && (
                                <Text size="xs" c="dimmed">
                                    {formatFileSize(fileInfo.fileSize)} • {fileInfo.fileType}
                                </Text>
                            )}
                        </Box>
                    </Group>
                    
                    {/* 操作按钮 */}
                    <Group gap="xs">
                        <Tooltip label="重新选择">
                            <Button
                                size="xs"
                                variant="outline"
                                onClick={handleFileSelect}
                                disabled={uploading}
                            >
                                重新选择
                            </Button>
                        </Tooltip>
                        <Tooltip label="删除">
                            <ActionIcon
                                variant="outline"
                                size="sm"
                                color="red"
                                onClick={handleClear}
                                disabled={uploading}
                            >
                                <Trash2 size={14} />
                            </ActionIcon>
                        </Tooltip>
                    </Group>
                </Stack>
            ) : (
                // 无文件时显示上传区域
                <Box
                    style={{
                        border: '2px dashed #ced4da',
                        borderRadius: '8px',
                        padding: '20px',
                        textAlign: 'center',
                        cursor: uploading ? 'default' : 'pointer',
                        transition: 'all 0.2s ease',
                        backgroundColor: '#f8f9fa',
                    }}
                    onClick={handleFileSelect}
                    onMouseEnter={(e) => {
                        if (!uploading) {
                            e.currentTarget.style.backgroundColor = '#e9ecef';
                            e.currentTarget.style.borderColor = '#adb5bd';
                        }
                    }}
                    onMouseLeave={(e) => {
                        if (!uploading) {
                            e.currentTarget.style.backgroundColor = '#f8f9fa';
                            e.currentTarget.style.borderColor = '#ced4da';
                        }
                    }}
                >
                    {uploading ? (
                        <>
                            <Loader size={20} />
                            <Text size="sm" c="dimmed" mt="xs">
                                上传中...
                            </Text>
                        </>
                    ) : (
                        <>
                            <Upload size={24} color="#adb5bd" style={{ margin: '0 auto' }} />
                            <Text size="sm" c="dimmed" mt="xs">
                                点击选择文件
                            </Text>
                            <Text size="xs" c="dimmed" mt="xs">
                                支持 {accept || '所有文件类型'}，文件大小不超过{' '}
                                {formatFileSize(maxSize || 10 * 1024 * 1024)}
                            </Text>
                        </>
                    )}
                </Box>
            )}

            {/* 上传状态提示 */}
            {uploading && (
                <Text size="xs" c="blue">
                    文件上传中，请稍候...
                </Text>
            )}

            {/* 错误提示 */}
            {uploadError && (
                <Text size="sm" c="red">
                    {uploadError}
                </Text>
            )}
        </Stack>
    );
}