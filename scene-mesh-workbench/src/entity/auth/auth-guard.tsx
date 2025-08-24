"use client";

import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import { isAuthenticated, getCurrentUser } from "./auth-utils";
import { Container, Loader, Center, Text } from "@mantine/core";

interface AuthGuardProps {
  children: React.ReactNode;
  requireAuth?: boolean;
  requireAdmin?: boolean;
}

/**
 * 认证守卫组件
 * 根据认证状态控制页面访问
 */
export function AuthGuard({
  children,
  requireAuth = true,
  requireAdmin = false,
}: AuthGuardProps) {
  const router = useRouter();
  const [isChecking, setIsChecking] = useState(true);
  const [hasAccess, setHasAccess] = useState(false);
  const [isRedirecting, setIsRedirecting] = useState(false);

  useEffect(() => {
    const checkAuth = () => {
      const authenticated = isAuthenticated();
      const user = getCurrentUser();

      // 如果不需要认证，直接允许访问
      if (!requireAuth) {
        setHasAccess(true);
        setIsChecking(false);
        return;
      }

      // 如果需要认证但用户未登录，重定向到登录页
      if (!authenticated || !user) {
        setIsRedirecting(true);
        router.push("/auth");
        return;
      }

      // 如果需要管理员权限但用户不是管理员
      if (requireAdmin && user.role !== "admin") {
        setHasAccess(false);
        setIsChecking(false);
        return;
      }

      // 检查用户状态
      if (user.status !== "active") {
        setIsRedirecting(true);
        router.push("/auth");
        return;
      }

      setHasAccess(true);
      setIsChecking(false);
    };

    checkAuth();
  }, [router, requireAuth, requireAdmin]);

  if (isChecking || isRedirecting) {
    return (
      <Container size="sm" style={{ height: "100vh" }}>
        <Center style={{ height: "100%" }}>
          <div style={{ textAlign: "center" }}>
            <Loader size="lg" />
            <Text mt="md" size="sm" color="dimmed">
              {isRedirecting ? "正在跳转到登录页..." : "正在验证身份..."}
            </Text>
          </div>
        </Center>
      </Container>
    );
  }

  if (!hasAccess) {
    return (
      <Container size="sm" style={{ height: "100vh" }}>
        <Center style={{ height: "100%" }}>
          <div style={{ textAlign: "center" }}>
            <Text size="lg" fw={500}>
              访问被拒绝
            </Text>
            <Text mt="md" size="sm" color="dimmed">
              您没有权限访问此页面
            </Text>
          </div>
        </Center>
      </Container>
    );
  }

  return <>{children}</>;
}
