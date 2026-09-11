import { describe, it, expect, vi, beforeEach } from "vitest";
import {
  axiosClient,
  request,
} from "../../main/ts/infrastructure/adapter/middleware/routes/internal-client";
import {
  HttpMethod,
  ErrorResponse,
} from "munchies-commons/kotlin/commons-modules";

describe("internal-client.ts", () => {
  beforeEach(() => {
    vi.restoreAllMocks();
  });

  it("checks axiosClient configuration and interceptors", () => {
    // Check transformRequest and transformResponse
    const transformReq = axiosClient.defaults.transformRequest;
    if (Array.isArray(transformReq) && transformReq[0]) {
      expect(transformReq[0]("sample-data", {} as any)).toBe("sample-data");
    }

    const transformRes = axiosClient.defaults.transformResponse;
    if (Array.isArray(transformRes) && transformRes[0]) {
      expect(transformRes[0]("sample-res")).toBe("sample-res");
    }

    // Check validateStatus
    const validateStatus = axiosClient.defaults.validateStatus;
    if (validateStatus) {
      expect(validateStatus(200)).toBe(true);
      expect(validateStatus(500)).toBe(true);
      expect(validateStatus(501)).toBe(false);
    }

    // Test request/response interceptors directly
    const reqHandlers = (axiosClient.interceptors.request as any).handlers;
    for (const h of reqHandlers) {
      if (h && h.fulfilled) {
        const config = { data: "test-data" } as any;
        expect(h.fulfilled(config)).toBe(config);
      }
    }

    const resHandlers = (axiosClient.interceptors.response as any).handlers;
    for (const h of resHandlers) {
      if (h && h.fulfilled) {
        const resp = { data: "test-response" } as any;
        expect(h.fulfilled(resp)).toBe(resp);
      }
    }
  });

  it("handles GET request with status 200", async () => {
    vi.spyOn(axiosClient, "get").mockResolvedValue({
      status: 200,
      data: JSON.stringify({ message: "ok" }),
    } as any);

    const parseRes = vi.fn((json: string) => JSON.parse(json));
    const parseErr = vi.fn((json: string) => new ErrorResponse("err", 400));

    const result = await request(
      "http://example.com/api",
      HttpMethod.GET,
      "",
      parseRes,
      parseErr
    );

    expect(axiosClient.get).toHaveBeenCalledWith("http://example.com/api");
    expect(parseRes).toHaveBeenCalled();
    expect(parseErr).not.toHaveBeenCalled();
    expect(result).toEqual({ message: "ok" });
  });

  it("handles POST request with status 200", async () => {
    vi.spyOn(axiosClient, "post").mockResolvedValue({
      status: 200,
      data: '{"created":true}',
    } as any);

    const parseRes = vi.fn((json: string) => JSON.parse(json));
    const parseErr = vi.fn();

    const result = await request(
      "http://example.com/api",
      HttpMethod.POST,
      '{"name":"test"}',
      parseRes,
      parseErr
    );

    expect(axiosClient.post).toHaveBeenCalledWith("http://example.com/api", '{"name":"test"}');
    expect(result).toEqual({ created: true });
  });

  it("handles PUT request with status 200", async () => {
    vi.spyOn(axiosClient, "put").mockResolvedValue({
      status: 200,
      data: '{"updated":true}',
    } as any);

    const parseRes = vi.fn((json: string) => JSON.parse(json));
    const parseErr = vi.fn();

    const result = await request(
      "http://example.com/api",
      HttpMethod.PUT,
      '{"name":"updated"}',
      parseRes,
      parseErr
    );

    expect(axiosClient.put).toHaveBeenCalledWith("http://example.com/api", '{"name":"updated"}');
    expect(result).toEqual({ updated: true });
  });

  it("handles DELETE request with status 200", async () => {
    vi.spyOn(axiosClient, "delete").mockResolvedValue({
      status: 200,
      data: '{"deleted":true}',
    } as any);

    const parseRes = vi.fn((json: string) => JSON.parse(json));
    const parseErr = vi.fn();

    const result = await request(
      "http://example.com/api",
      HttpMethod.DELETE,
      '{"id":"1"}',
      parseRes,
      parseErr
    );

    expect(axiosClient.delete).toHaveBeenCalledWith("http://example.com/api", { data: '{"id":"1"}' });
    expect(result).toEqual({ deleted: true });
  });

  it("handles PATCH request with status 200", async () => {
    vi.spyOn(axiosClient, "patch").mockResolvedValue({
      status: 200,
      data: '{"patched":true}',
    } as any);

    const parseRes = vi.fn((json: string) => JSON.parse(json));
    const parseErr = vi.fn();

    const result = await request(
      "http://example.com/api",
      HttpMethod.PATCH,
      '{"key":"val"}',
      parseRes,
      parseErr
    );

    expect(axiosClient.patch).toHaveBeenCalledWith("http://example.com/api", '{"key":"val"}');
    expect(result).toEqual({ patched: true });
  });

  it("handles error response when status >= 400", async () => {
    vi.spyOn(axiosClient, "get").mockResolvedValue({
      status: 404,
      data: "Not Found",
    } as any);

    const parseRes = vi.fn();
    const parseErr = vi.fn((json: string) => new ErrorResponse("Not found err", 404));

    const result = await request(
      "http://example.com/api/missing",
      HttpMethod.GET,
      "",
      parseRes,
      parseErr
    );

    expect(parseErr).toHaveBeenCalledWith("Not Found");
    expect(parseRes).not.toHaveBeenCalled();
    expect(result).toBeInstanceOf(ErrorResponse);
    expect((result as ErrorResponse).code).toBe(404);
  });

  it("catches axios network/request errors and returns 500 ErrorResponse", async () => {
    vi.spyOn(axiosClient, "get").mockRejectedValue(new Error("Network Connection Refused"));

    const parseRes = vi.fn();
    const parseErr = vi.fn();

    const result = await request(
      "http://example.com/api/down",
      HttpMethod.GET,
      "",
      parseRes,
      parseErr
    );

    expect(result).toBeInstanceOf(ErrorResponse);
    expect((result as ErrorResponse).code).toBe(500);
    expect((result as any).result).toContain("Internal Axios Request:");
    expect((result as any).result).toContain("Network Connection Refused");
  });
});
