import { NextRequest, NextResponse } from 'next/server';

export async function POST(request: NextRequest) {
    const body = await request.json();
    console.log('==========body:', JSON.stringify(body));
    const computeServerBaseUrl = process.env.COMPUTE_SERVER_BASE_URL;
    const computeServerVectorizationApi = process.env.COMPUTE_SERVER_VECTORIZATION_SUBMIT_API;
    const vectorizationUrl = `${computeServerBaseUrl}${computeServerVectorizationApi}`;

    try {
        const response = await fetch(`${vectorizationUrl}`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({
                "taskType": "vectorization",
                "taskData": body
            }),
        });

        return NextResponse.json(await response.json());
    } catch (error) {
        return NextResponse.json(
            { error: error instanceof Error ? error.message : 'Unknown error' },
            { status: 500 }
        );
    }
}

export async function GET(request: NextRequest) {
    const { searchParams } = new URL(request.url);
    const knowledgeBaseId = searchParams.get('knowledgeBaseId');
    const knowledgeItemId = searchParams.get('knowledgeItemId');
    const providerName = searchParams.get('providerName');
    const modelName = searchParams.get('modelName');
    const page = searchParams.get('page');
    const size = searchParams.get('size');

    const computeServerBaseUrl = process.env.COMPUTE_SERVER_BASE_URL;
    const computeServerVectorizationFindApi = process.env.COMPUTE_SERVER_VECTORIZATION_FIND_API;
    const vectorizationUrl = `${computeServerBaseUrl}${computeServerVectorizationFindApi}`;

    const queryParams = new URLSearchParams({
        knowledgeBaseId: knowledgeBaseId || '',
        knowledgeItemId: knowledgeItemId || '',
        providerName: providerName || '',
        modelName: modelName || '',
        page: page || '',
        size: size || ''
    });
        
    try {
        const response = await fetch(`${vectorizationUrl}?${queryParams}`, {
            method: 'GET'
        });

        return NextResponse.json(await response.json());
    } catch (error) {
        return NextResponse.json(
            { error: error instanceof Error ? error.message : 'Unknown error' },
            { status: 500 }
        );
    }
}

export async function DELETE(request: NextRequest) {
    const { searchParams } = new URL(request.url);
    const knowledgeBaseId = searchParams.get('knowledgeBaseId');
    const knowledgeItemId = searchParams.get('knowledgeItemId');
    const providerName = searchParams.get('providerName');
    const modelName = searchParams.get('modelName');

    const computeServerBaseUrl = process.env.COMPUTE_SERVER_BASE_URL;
    const computeServerVectorizationFindApi = process.env.COMPUTE_SERVER_VECTORIZATION_FIND_API;
    const vectorizationUrl = `${computeServerBaseUrl}${computeServerVectorizationFindApi}`;

    const queryParams = new URLSearchParams({
        knowledgeBaseId: knowledgeBaseId || '',
        knowledgeItemId: knowledgeItemId || '',
        providerName: providerName || '',
        modelName: modelName || ''
    });
        
    try {
        const response = await fetch(`${vectorizationUrl}?${queryParams}`, {
            method: 'DELETE'
        });

        return NextResponse.json(await response.json());
    } catch (error) {
        return NextResponse.json(
            { error: error instanceof Error ? error.message : 'Unknown error' },
            { status: 500 }
        );
    }
}