"use client";

import { useRouter } from "next/navigation";
import {
  EntityViewInspector,
  createEntityEngineProvider,
  EntityPermissionActionType,
} from "@scenemesh/entity-engine";

import { views, models } from "../model-config";
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import { hasPermission, isAuthenticated, getCurrentUser } from "../auth/auth-utils";

type EntityEngineProviderWrapperProps = {
  children: React.ReactNode;
};

export function EntityEngineProviderWrapper(
  props: EntityEngineProviderWrapperProps,
) {
  const router = useRouter();

  const EntityEngineProvider = createEntityEngineProvider({
    models,
    views,
    suiteAdapters: [],
    suiteAdapter: { suiteName: "build-in", suiteVersion: "1.0.0" },
    router: {
      navigate: (path: string, state?: unknown) => {
        // eslint-disable-next-line no-console
        console.log(`Navigating to ${path} with state:`, state);
        router.push(path, undefined);
      },
    },
    permissionGuard: {
      checkPermission: async (action: EntityPermissionActionType) => {
        // eslint-disable-next-line no-console
        console.log("Checking permission for action:", action);
        return true;
        
        // // 公开的认证相关操作，无需登录
        // const publicActions = ["auth:login", "auth:register", "auth:logout"];
        // const actionString = String(action);
        // if (publicActions.some(pa => actionString.includes(pa))) {
        //   return true;
        // }

        // // 检查用户是否已登录
        // if (!isAuthenticated()) {
        //   // eslint-disable-next-line no-console
        //   console.log("User not authenticated, denying permission");
        //   return false;
        // }

        // // 获取当前用户信息
        // const currentUser = getCurrentUser();
        // if (!currentUser || currentUser.status !== "active") {
        //   // eslint-disable-next-line no-console
        //   console.log("User inactive or not found, denying permission");
        //   return false;
        // }

        // // 管理员拥有所有权限
        // if (currentUser.role === "admin") {
        //   return true;
        // }

        // // 使用权限检查函数
        // const permissionKey = `${action}`;
        // const hasAccess = hasPermission(permissionKey);
        
        // // eslint-disable-next-line no-console
        // console.log(`Permission check for ${permissionKey}: ${hasAccess}`);
        // return hasAccess;
      },
    },
    renderers: [
      {
        name: 'shell-settings',
        slotName: 'shell-settings-item',
        renderer: (_p: unknown) => {
  
          return (
          <div>
            Settings Component
          </div>
          );
        },
      },
      {
        name: 'view-tool-2',
        slotName: 'view-tool',
        renderer: (p: any) => <div>测试{p.model.name}</div>,
      },
      {
        ...EntityViewInspector,
      },
    ],
    // Note: customActionHandlers may need to be implemented differently based on entity-engine version
    // For now, we'll handle logout through the permission guard system
    serverInfo: {
      baseUrl: process.env.NEXT_PUBLIC_API_BASE_URL || "http://localhost:3000",
      endpoint: process.env.NEXT_PUBLIC_API_ENDPOINT || "/api/ee",
    },
  });

  return <EntityEngineProvider>{props.children}</EntityEngineProvider>;
}

