'use client';

import { EntityViewContainer } from "@scenemesh/entity-engine";

export default function IndexPage() {
  return (
    <EntityViewContainer
      modelName="product"
      viewType="shell"
      viewName="mainDashboardView"
      baseObjectId="z1cv5emekgc0jpoyue65n5c4"
    />
  )
}
