import type { EdgeProps } from "@xyflow/react";

import React from "react";
// eslint-disable-next-line no-duplicate-imports
import { BaseEdge, useReactFlow, getBezierPath } from "@xyflow/react";

// 引入 Box 用于样式化

const ComputeEdge: React.FC<EdgeProps> = ({
  sourceX,
  sourceY,
  targetX,
  targetY,
  sourcePosition,
  targetPosition,
  style = {},
  markerEnd,
}) => {
  useReactFlow();

  const [edgePath] = getBezierPath({
    sourceX,
    sourceY,
    sourcePosition,
    targetX,
    targetY,
    targetPosition,
  });

  const edgeStyle = {
    ...style,
    strokeDasharray: "5 5",
    strokeWidth: 2,
  };

  return <BaseEdge path={edgePath} markerEnd={markerEnd} style={edgeStyle} />;
};

export default React.memo(ComputeEdge);
