// common-modules.d.ts
import type { com } from "./munchies-commons";

export type UUIDEntityId = com.munchies.commons.UUIDEntityId;
export declare const UUIDEntityId: typeof com.munchies.commons.UUIDEntityId;

export declare const newId = com.munchies.commons.UUIDEntityId.Companion.newId