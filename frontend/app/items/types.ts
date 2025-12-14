// types.ts (ou onde tiveres as tuas interfaces)
export interface Item {
  id: number;
  name: string;
  description: string;
  material?: string;
  category?: string;
  price?: number;
  images?: string[];
  photoUrls?: string[];
  available?: boolean;
  owner?: {
    id: number;
    name?: string;
    email?: string;
  };
}

export type CategoryNode = {
  id: string;
  displayName: string;
  topLevel: boolean;
  subCategories: CategoryNode[];
};

export type FlatCategory = CategoryNode & {
  level: number;
  rootId: string;
};

export type MaterialMap = Record<string, string[]>;

export type ItemFilter = {
  name?: string | null;
  category?: string | null;
  material?: string | null;
  minPrice?: number | null;
  maxPrice?: number | null;
};
