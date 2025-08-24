import { NextRequest, NextResponse } from "next/server";

/**
 * 登录API端点（模拟实现）
 * 在实际项目中，这里应该连接到真实的数据库和认证系统
 */
export async function POST(request: NextRequest) {
  try {
    const { username, password } = await request.json();

    // 模拟验证延迟
    await new Promise((resolve) => setTimeout(resolve, 1000));

    // 简单的模拟用户数据
    const mockUsers = [
      {
        id: "1",
        username: "admin",
        email: "admin@example.com",
        role: "admin",
        status: "active",
        avatar: null,
      },
      {
        id: "2",
        username: "user",
        email: "user@example.com",
        role: "user",
        status: "active",
        avatar: null,
      },
    ];

    // 查找用户（支持用户名或邮箱登录）
    const user = mockUsers.find(
      (u) =>
        (u.username === username || u.email === username) &&
        password === "123456" // 简单的密码验证
    );

    if (!user) {
      return NextResponse.json(
        { success: false, error: "用户名或密码错误" },
        { status: 401 }
      );
    }

    if (user.status !== "active") {
      return NextResponse.json(
        { success: false, error: "账户已被禁用" },
        { status: 403 }
      );
    }

    // 生成简单的token（实际项目中应该使用JWT）
    const token = `token_${user.id}_${Date.now()}`;

    // 返回用户信息和token
    return NextResponse.json({
      success: true,
      user: {
        ...user,
        lastLoginAt: new Date().toISOString(),
      },
      token,
    });
  } catch (error) {
    console.error("Login error:", error);
    return NextResponse.json(
      { success: false, error: "服务器错误" },
      { status: 500 }
    );
  }
}
