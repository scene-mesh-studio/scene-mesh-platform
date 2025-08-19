'use client';

import { useRouter } from 'next/navigation';
import { EntityViewInspector, createEntityEngineProvider, EntityPermissionActionType } from '@scenemesh/entity-engine';

import { views, models } from '../model-config';

type EntityEngineProviderWrapperProps = {
  children: React.ReactNode;
};

export function EntityEngineProviderWrapper(props: EntityEngineProviderWrapperProps) {
  const router = useRouter();

  const EntityEngineProvider = createEntityEngineProvider({
    models,
    views,
    suiteAdapters: [],
    suiteAdapter: { suiteName: 'build-in', suiteVersion: '1.0.0' },
    router: {
      navigate: (path: string, state?: any) => {
        console.log(`Navigating to ${path} with state:`, state);
        router.push(path, undefined);
      },
    },
    permissionGuard: {
      checkPermission: async (action: EntityPermissionActionType) => {
        // 这里可以添加权限检查逻辑
        console.log(`Checking permission for action: ${action}`);
        return true;
      },
    },
    renderers: [
      {
        ...EntityViewInspector,
      },
    ],
    serverInfo: {
      baseUrl: process.env.NEXT_PUBLIC_API_BASE_URL || 'http://localhost:3000',
      endpoint: process.env.NEXT_PUBLIC_API_ENDPOINT || '/api/ee',
    },
  });

  return (
    <EntityEngineProvider>{props.children}</EntityEngineProvider>
  );
}
