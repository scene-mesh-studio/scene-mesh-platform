import '@scenemesh/entity-engine/main.css';
import type { Metadata } from "next";
import { Geist, Geist_Mono } from "next/font/google";
import "./globals.css";
import { EntityEngineProviderWrapper } from "../entity/provider/entity-engine-provider-warpper";
import { Notifications } from "@mantine/notifications";

const geistSans = Geist({
  variable: "--font-geist-sans",
  subsets: ["latin"],
});

const geistMono = Geist_Mono({
  variable: "--font-geist-mono",
  subsets: ["latin"],
});

export const metadata: Metadata = {
  title: "智能物联网平台",
  description: "基于Entity Engine的智能物联网管理平台",
  icons: {
    icon: [
      { url: "/images/image.png", sizes: "32x32", type: "image/png" },
    ],
  },
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="en">
      <body
        className={`${geistSans.variable} ${geistMono.variable} antialiased`}
      >
        <EntityEngineProviderWrapper>
          {children}
          <Notifications />
        </EntityEngineProviderWrapper>
      </body>
    </html>
  );
}
