import React from 'react';

import { EntityWidgetProps, useEntityEngine } from "@scenemesh/entity-engine";
import { FileUploadComp } from "./file-upload-comp";


export function FileUploadCompWrapper(props: EntityWidgetProps) {
    const { model, field } = props;
    const engine = useEntityEngine();

    const accept = field.widgetOptions?.accept || '*/*';
    const maxSize = field.widgetOptions?.maxSize || 30 * 1024 * 1024; // 默认5MB
    const showInfo = field.widgetOptions?.showInfo ?? true; // 是否显示上传信息

    const handleFileUpload = async (file: File) => {

        // 通过request multipart/form-data上传文件
        const formData = new FormData();
        formData.append('file', file);
        formData.append('modelName', model?.name || 'default'); // 修复拼写错误

        // 上传文件
        // 这里可以使用fetch或axios等库进行文件上传 /api/ee/utils/upload
        const response = await fetch(engine.settings.getUrl('/utils/upload'), {
            method: 'POST',
            body: formData,
        });
        if (!response.ok) {
            throw new Error('File upload failed');
        }

        const ret = await response.json();

        return ret;
    };

    return <FileUploadComp
        {...props}
        onFileUpload={handleFileUpload}
        multiple={false}
        accept={accept as string}
        maxSize={maxSize as number}
        showInfo={showInfo as boolean} />;
}