export type Item = {
  id: string;
  name: string;
  description: string;
  material?: string;
  category?: string;
  price?: number;
  photoUrls?: string[];
};

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
