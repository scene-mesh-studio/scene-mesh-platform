import type { NextRequest } from "next/server";
import { models, views } from "../../../..//entity/model-config";
import {
  EnginePrimitiveInitializer,
  fetchEntityEntranceHandler,
} from "@scenemesh/entity-engine/server";

const rootPath = "/api/ee";

const handler = async (req: NextRequest) => {
  console.log("Entity Engine TRPC Handler: ", req.url);

  try {
    const init = new EnginePrimitiveInitializer({models, views});

    return fetchEntityEntranceHandler({
      request: req,
      endpoint: rootPath,
      initializer: init,
    });
  } catch (error) {
    console.error("Error in entity engine handler:", error);
    return new Response(
      JSON.stringify({
        error: "Entity Engine handler error",
        message: error instanceof Error ? error.message : "Unknown error",
      }),
      {
        headers: { "Content-Type": "application/json" },
        status: 500,
      },
    );
  }
};

export { handler as GET, handler as POST };
