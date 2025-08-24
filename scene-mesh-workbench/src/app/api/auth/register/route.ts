import { NextRequest, NextResponse } from "next/server";

/**
 * 注册API端点（模拟实现）
 * 在实际项目中，这里应该连接到真实的数据库
 */
export async function POST(request: NextRequest) {
  try {
    const { username, email, password } = await request.json();

    // 模拟API调用延迟
    await new Promise((resolve) => setTimeout(resolve, 1000));

    // 基本验证
    if (!username || username.length < 3) {
      return NextResponse.json(
        { success: false, error: "用户名至少3个字符" },
        { status: 400 }
      );
    }

    if (!password || password.length < 6) {
      return NextResponse.json(
        { success: false, error: "密码至少6个字符" },
        { status: 400 }
      );
    }

    if (!email || !/\S+@\S+\.\S+/.test(email)) {
      return NextResponse.json(
        { success: false, error: "请输入有效的邮箱地址" },
        { status: 400 }
      );
    }

    // 模拟检查用户名/邮箱是否已存在
    const existingUsers = ["admin", "user", "admin@example.com", "user@example.com"];
    if (existingUsers.includes(username) || existingUsers.includes(email)) {
      return NextResponse.json(
        { success: false, error: "用户名或邮箱已存在" },
        { status: 409 }
      );
    }

    // 创建新用户
    const newUser = {
      id: `user_${Date.now()}`,
      username,
      email,
      role: "user" as const,
      status: "active" as const,
      avatar: null,
      createdAt: new Date().toISOString(),
      lastLoginAt: new Date().toISOString(),
    };

    // 生成token
    const token = `token_${newUser.id}_${Date.now()}`;

    return NextResponse.json({
      success: true,
      user: newUser,
      token,
    });
  } catch (error) {
    console.error("Register error:", error);
    return NextResponse.json(
      { success: false, error: "服务器错误" },
      { status: 500 }
    );
  }
}
