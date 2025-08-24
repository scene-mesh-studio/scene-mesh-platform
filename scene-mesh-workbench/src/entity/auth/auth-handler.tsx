"use client";

import { useRouter } from "next/navigation";
import { notifications } from "@mantine/notifications";
import { login, register, logout as authLogout } from "./auth-utils";

/**
 * 认证处理器
 * 处理登录、注册、登出等操作
 */
export class AuthHandler {
  private router: ReturnType<typeof useRouter>;

  constructor(router: ReturnType<typeof useRouter>) {
    this.router = router;
  }

  /**
   * 处理登录
   */
  async handleLogin(formData: { username: string; password: string }) {
    try {
      const result = await login(formData);

      if (result.success && result.user) {
        notifications.show({
          title: "登录成功",
          message: `欢迎回来，${result.user.username}！`,
          color: "green",
        });

        // 重定向到主页面
        this.router.push("/");
        return { success: true };
      } else {
        notifications.show({
          title: "登录失败",
          message: result.error || "未知错误",
          color: "red",
        });
        return { success: false, error: result.error };
      }
    } catch {
      const errorMessage = "登录过程中发生错误";
      notifications.show({
        title: "登录失败",
        message: errorMessage,
        color: "red",
      });
      return { success: false, error: errorMessage };
    }
  }

  /**
   * 处理注册
   */
  async handleRegister(formData: {
    username: string;
    email: string;
    password: string;
  }) {
    try {
      const result = await register(formData);

      if (result.success && result.user) {
        notifications.show({
          title: "注册成功",
          message: `欢迎加入，${result.user.username}！`,
          color: "green",
        });

        // 重定向到主页面
        this.router.push("/");
        return { success: true };
      } else {
        notifications.show({
          title: "注册失败",
          message: result.error || "未知错误",
          color: "red",
        });
        return { success: false, error: result.error };
      }
    } catch {
      const errorMessage = "注册过程中发生错误";
      notifications.show({
        title: "注册失败",
        message: errorMessage,
        color: "red",
      });
      return { success: false, error: errorMessage };
    }
  }

  /**
   * 处理登出
   */
  handleLogout() {
    try {
      authLogout();
      notifications.show({
        title: "已退出登录",
        message: "感谢您的使用！",
        color: "blue",
      });

      // 重定向到登录页面
      this.router.push("/auth");
    } catch {
      notifications.show({
        title: "退出登录失败",
        message: "退出登录时发生错误",
        color: "red",
      });
    }
  }
}

/**
 * 创建认证处理器的Hook
 */
export function useAuthHandler() {
  const router = useRouter();
  return new AuthHandler(router);
}
