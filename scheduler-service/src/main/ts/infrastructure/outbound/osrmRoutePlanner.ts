import axios, { AxiosInstance } from "axios";
import { GeoPoint } from "../../domain/model/geo";
import { RoutePlanner } from "../../domain/ports/routePlanner";

interface OsrmStep {
  geometry?: { coordinates?: [number, number][] };
}

interface OsrmResponse {
  code?: string;
  message?: string;
  routes?: { legs?: { steps?: OsrmStep[] }[] }[];
}

/**
 * Plans routes with an OSRM server (Open Source Routing Machine, on OpenStreetMap data), using
 * its Route service: GET /route/v1/driving/{lng,lat;lng,lat;...}. One request covers the whole
 * trip; `steps=true` is what makes OSRM return the geometry leg by leg, and it is needed to
 * know where along the route each stop is. OSRM speaks "longitude,latitude", the opposite of
 * GeoPoint's field order.
 */
export class OsrmRoutePlanner implements RoutePlanner {
  private readonly http: AxiosInstance;

  constructor(baseUrl: string, http?: AxiosInstance) {
    this.http =
      http ??
      axios.create({
        baseURL: baseUrl,
        // The manager is waiting on the advance that triggers this: fail fast, fall back.
        timeout: 5_000,
        validateStatus: () => true,
      });
  }

  async planLegs(waypoints: GeoPoint[]): Promise<GeoPoint[][]> {
    if (waypoints.length < 2) throw new RangeError("a route needs at least two waypoints");
    const coordinates = waypoints.map((p) => `${p.lng},${p.lat}`).join(";");
    const res = await this.http.get<OsrmResponse>(`/route/v1/driving/${coordinates}`, {
      params: { steps: true, geometries: "geojson", overview: "false" },
    });
    const body = res.data;
    if (res.status !== 200 || body?.code !== "Ok") {
      throw new Error(`OSRM could not plan the route: ${body?.message ?? body?.code ?? `HTTP ${res.status}`}`);
    }
    const legs = body.routes?.[0]?.legs ?? [];
    if (legs.length !== waypoints.length - 1) {
      throw new Error(`OSRM returned ${legs.length} legs for ${waypoints.length} waypoints`);
    }
    return legs.map((leg, i) => {
      const points = (leg.steps ?? []).flatMap((step) =>
        (step.geometry?.coordinates ?? []).map(([lng, lat]) => ({ lat, lng })),
      );
      // Two waypoints snapped to the same spot give a leg with nothing to drive.
      return points.length > 0 ? points : [{ ...waypoints[i] }];
    });
  }
}
