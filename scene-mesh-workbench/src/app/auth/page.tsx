"use client";

import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import { 
  Container, 
  Paper, 
  Title, 
  Text, 
  Tabs, 
  TabsList, 
  TabsTab, 
  TabsPanel,
  Stack,
  Button,
  Divider
} from "@mantine/core";
import { Icon } from "@iconify/react";
import { isAuthenticated } from "@/entity/auth/auth-utils";
import { EntityViewContainer } from "@scenemesh/entity-engine";

/**
 * 认证页面
 * 如果用户已登录，重定向到主页面
 * 否则显示登录/注册表单
 */
export default function AuthPage() {
  const router = useRouter();
  const [activeTab, setActiveTab] = useState<string | null>("login");

  useEffect(() => {
    // 如果用户已登录，重定向到主页面
    if (isAuthenticated()) {
      router.push("/");
    }
  }, [router]);

  // 如果已登录，不渲染任何内容（将重定向）
  if (isAuthenticated()) {
    return null;
  }

  return (
    <Container size="xs" mt="xl">
      <Paper shadow="md" p="xl" radius="md">
        <Stack align="center" mb="xl">
          <Icon 
            icon="mdi:apple-keyboard-command" 
            width={48} 
            height={48}
            color="var(--mantine-color-blue-6)"
          />
          <Title order={1} size="h3" ta="center">
            智能物联网平台
          </Title>
          <Text c="dimmed" ta="center">
            请登录或注册您的账户
          </Text>
        </Stack>

        <Tabs value={activeTab} onChange={setActiveTab}>
          <TabsList grow mb="md">
            <TabsTab value="login">登录</TabsTab>
            <TabsTab value="register">注册</TabsTab>
          </TabsList>

          <TabsPanel value="login">
            <EntityViewContainer
              modelName="user"
              viewType="form"
              viewName="loginFormView"
              behavior={{
                mode: "edit",
                toCreating: true,
                readonly: true,
              }}
              baseObjectId=""
              callbacks={{
                onObjectCreated: (_user) => {
                  // 登录成功后重定向
                  router.push("/");
                },
                onObjectUpdated: (_user) => {
                  // 登录成功后重定向
                  router.push("/");
                }
              }}
            />
          </TabsPanel>

          <TabsPanel value="register">
            <EntityViewContainer
              modelName="user"
              viewType="form"
              viewName="registerFormView"
              baseObjectId=""
              callbacks={{
                onObjectCreated: (_user) => {
                  // 注册成功后切换到登录页
                  setActiveTab("login");
                },
                onObjectUpdated: (_user) => {
                  // 注册成功后切换到登录页
                  setActiveTab("login");
                }
              }}
            />
          </TabsPanel>
        </Tabs>

        <Divider my="md" label="或者" labelPosition="center" />

        <Stack gap="xs">
          <Button
            variant="light"
            onClick={() => setActiveTab(activeTab === "login" ? "register" : "login")}
            fullWidth
          >
            {activeTab === "login" ? "没有账户？立即注册" : "已有账户？立即登录"}
          </Button>
        </Stack>
      </Paper>
    </Container>
  );
}
