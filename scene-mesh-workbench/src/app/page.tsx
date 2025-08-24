"use client";

import { EntityViewContainer } from "@scenemesh/entity-engine";
import { AuthGuard } from "@/entity/auth/auth-guard";

export default function IndexPage() {
  return (
    <AuthGuard requireAuth={false}>
      <EntityViewContainer
        modelName="product"
        viewType="shell"
        viewName="mainDashboardView"
      />
    </AuthGuard>
  );
}
