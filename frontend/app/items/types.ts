// types.ts (ou onde tiveres as tuas interfaces)
export interface Item {
  id: number;
  name: string;
  description: string;
  photoUrls: string[]; // In Java, this is List<String>
  category: string; // Enum in Java, string here
  material: string; // Enum in Java, string here
  price: number;
  available: boolean;
  userId: number;
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
