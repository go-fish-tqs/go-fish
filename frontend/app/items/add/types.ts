export interface Category {
    id: string;
    name: string;
}

export interface Material {
    id: string;
    name: string;
}

export interface MaterialGroup {
    group: string;
    items: Material[];
}