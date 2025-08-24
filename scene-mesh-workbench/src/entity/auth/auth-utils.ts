/**
 * 认证工具函数
 * 使用简单的localStorage进行会话管理
 * 遵循奥卡姆剃刀原则，实现最简洁的认证机制
 */

export interface User {
  id: string;
  username: string;
  email: string;
  role: "admin" | "user";
  status: "active" | "inactive" | "banned";
  avatar?: string;
  createdAt?: string;
  lastLoginAt?: string;
}

export interface AuthState {
  isAuthenticated: boolean;
  user: User | null;
  token: string | null;
}

const AUTH_STORAGE_KEY = "scene_mesh_auth";

/**
 * 获取当前认证状态
 */
export function getAuthState(): AuthState {
  if (typeof window === "undefined") {
    return { isAuthenticated: false, user: null, token: null };
  }

  try {
    const stored = localStorage.getItem(AUTH_STORAGE_KEY);
    if (!stored) {
      return { isAuthenticated: false, user: null, token: null };
    }

    const authState: AuthState = JSON.parse(stored);
    
    // 简单的token过期检查（实际项目中应该检查JWT过期时间）
    if (authState.token && authState.user) {
      return authState;
    }

    return { isAuthenticated: false, user: null, token: null };
  } catch {
    return { isAuthenticated: false, user: null, token: null };
  }
}

/**
 * 保存认证状态
 */
export function setAuthState(authState: AuthState): void {
  if (typeof window === "undefined") return;

  if (authState.isAuthenticated && authState.user && authState.token) {
    localStorage.setItem(AUTH_STORAGE_KEY, JSON.stringify(authState));
  } else {
    localStorage.removeItem(AUTH_STORAGE_KEY);
  }
}

/**
 * 清除认证状态
 */
export function clearAuthState(): void {
  if (typeof window === "undefined") return;
  localStorage.removeItem(AUTH_STORAGE_KEY);
}

/**
 * 获取当前用户
 */
export function getCurrentUser(): User | null {
  return getAuthState().user;
}

/**
 * 检查用户是否已登录
 */
export function isAuthenticated(): boolean {
  return getAuthState().isAuthenticated;
}

/**
 * 检查用户是否为管理员
 */
export function isAdmin(): boolean {
  const user = getCurrentUser();
  return user?.role === "admin" && user?.status === "active";
}

/**
 * 检查用户权限
 */
export function hasPermission(action: string): boolean {
  const user = getCurrentUser();
  if (!user || user.status !== "active") {
    return false;
  }

  // 公开的认证相关操作
  const publicActions = ["auth:login", "auth:register", "auth:logout"];
  if (publicActions.includes(action)) {
    return true;
  }

  // 管理员拥有所有权限
  if (user.role === "admin") {
    return true;
  }

  // 普通用户权限
  const userActions = [
    "user:read",
    "user:update-self",
    "product:read",
    "scene:read",
    "event:read",
    "action:read",
  ];

  return userActions.includes(action);
}

/**
 * 模拟登录（实际项目中应该调用API）
 */
export async function login(credentials: {
  username: string;
  password: string;
}): Promise<{ success: boolean; error?: string; user?: User }> {
  // 模拟API调用延迟
  await new Promise((resolve) => setTimeout(resolve, 1000));

  // 简单的模拟验证逻辑
  const mockUsers: User[] = [
    {
      id: "1",
      username: "admin",
      email: "admin@example.com",
      role: "admin",
      status: "active",
      createdAt: new Date().toISOString(),
      lastLoginAt: new Date().toISOString(),
    },
    {
      id: "2",
      username: "user",
      email: "user@example.com",
      role: "user",
      status: "active",
      createdAt: new Date().toISOString(),
      lastLoginAt: new Date().toISOString(),
    },
  ];

  const user = mockUsers.find(
    (u) =>
      (u.username === credentials.username || u.email === credentials.username) &&
      credentials.password === "123456", // 简单的密码验证
  );

  if (!user) {
    return { success: false, error: "用户名或密码错误" };
  }

  if (user.status !== "active") {
    return { success: false, error: "账户已被禁用" };
  }

  // 生成简单的token（实际项目中应该使用JWT）
  const token = `token_${user.id}_${Date.now()}`;

  const authState: AuthState = {
    isAuthenticated: true,
    user: { ...user, lastLoginAt: new Date().toISOString() },
    token,
  };

  setAuthState(authState);

  return { success: true, user };
}

/**
 * 模拟注册（实际项目中应该调用API）
 */
export async function register(userData: {
  username: string;
  email: string;
  password: string;
}): Promise<{ success: boolean; error?: string; user?: User }> {
  // 模拟API调用延迟
  await new Promise((resolve) => setTimeout(resolve, 1000));

  // 简单的验证
  if (userData.username.length < 3) {
    return { success: false, error: "用户名至少3个字符" };
  }

  if (userData.password.length < 6) {
    return { success: false, error: "密码至少6个字符" };
  }

  if (!/\S+@\S+\.\S+/.test(userData.email)) {
    return { success: false, error: "请输入有效的邮箱地址" };
  }

  // 模拟检查用户名/邮箱是否已存在
  const existingUsers = ["admin", "user", "admin@example.com", "user@example.com"];
  if (existingUsers.includes(userData.username) || existingUsers.includes(userData.email)) {
    return { success: false, error: "用户名或邮箱已存在" };
  }

  const newUser: User = {
    id: `user_${Date.now()}`,
    username: userData.username,
    email: userData.email,
    role: "user",
    status: "active",
    createdAt: new Date().toISOString(),
    lastLoginAt: new Date().toISOString(),
  };

  // 自动登录新注册用户
  const token = `token_${newUser.id}_${Date.now()}`;
  const authState: AuthState = {
    isAuthenticated: true,
    user: newUser,
    token,
  };

  setAuthState(authState);

  return { success: true, user: newUser };
}

/**
 * 登出
 */
export function logout(): void {
  clearAuthState();
  // 在实际项目中，这里应该调用API使服务端token失效
}
