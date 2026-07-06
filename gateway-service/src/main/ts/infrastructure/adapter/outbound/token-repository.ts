export interface TokenRepository<TokenId> {
  add(id: TokenId): void;
  isRevoked(id: TokenId): boolean;
  revoke(id: TokenId): void;
}

class MemoryTokenRepository implements TokenRepository<string> {
  private readonly repository: Map<string, boolean> = new Map();

  add(id: string): void {
    this.repository.set(id, true);
  }
  isRevoked(id: string): boolean {
    return this.repository.get(id) ?? false;
  }
  revoke(id: string): void {
    this.repository.set(id, false);
  }
}

const repository = new MemoryTokenRepository();

export function getTokenRepository(): TokenRepository<string> {
  return repository;
}
