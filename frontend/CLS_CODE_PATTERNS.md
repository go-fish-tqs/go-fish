# CLS Prevention - Code Patterns

## Copy-Paste Ready Patterns

### 1. Image with Dimensions

```tsx
// Prevent image load shift by specifying dimensions
<img
  src={imageUrl}
  alt="Description"
  width={400}
  height={300}
  className="w-full h-auto object-cover"
/>
```

### 2. Responsive Image with Aspect Ratio

```tsx
// Container defines aspect, image fills it
<div className="aspect-video w-full">
  <img
    src={imageUrl}
    alt="Description"
    className="w-full h-full object-cover"
  />
</div>

// Or use aspect-square, aspect-auto, etc.
```

### 3. Gallery Thumbnails with Fixed Size

```tsx
// Thumbnails with explicit dimensions
<button className="h-16 w-16 flex-shrink-0 overflow-hidden">
  <img
    src={thumbnailUrl}
    alt="Thumbnail"
    width={64}
    height={64}
    className="w-full h-full object-cover"
  />
</button>
```

### 4. Loading Skeleton (Grid)

```tsx
// Loading state that matches content layout
if (isLoading) {
  return (
    <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6">
      {Array.from({ length: 8 }).map((_, i) => (
        <div
          key={i}
          className="bg-white rounded-lg overflow-hidden shadow-lg animate-pulse"
        >
          <div className="h-48 bg-gray-200 flex-shrink-0" />
          <div className="p-4 space-y-3">
            <div className="h-4 bg-gray-200 rounded w-3/4" />
            <div className="h-3 bg-gray-200 rounded w-full" />
            <div className="h-3 bg-gray-200 rounded w-5/6" />
          </div>
        </div>
      ))}
    </div>
  );
}
```

### 5. Loading Skeleton (Detail Page)

```tsx
// Loading state for detail page with gallery
if (isLoading) {
  return (
    <div className="grid gap-6 lg:grid-cols-2">
      {/* Gallery skeleton */}
      <div className="space-y-4">
        <div className="aspect-square w-full bg-gray-200 rounded-lg animate-pulse flex-shrink-0" />
        <div className="flex gap-3">
          {Array.from({ length: 3 }).map((_, i) => (
            <div
              key={i}
              className="h-16 w-16 bg-gray-200 rounded animate-pulse flex-shrink-0"
            />
          ))}
        </div>
      </div>

      {/* Info skeleton */}
      <div className="space-y-4">
        <div className="h-8 bg-gray-200 rounded w-3/4 animate-pulse flex-shrink-0" />
        <div className="h-6 bg-gray-200 rounded w-1/4 animate-pulse flex-shrink-0" />
        {Array.from({ length: 4 }).map((_, i) => (
          <div
            key={i}
            className="h-4 bg-gray-200 rounded animate-pulse flex-shrink-0"
          />
        ))}
      </div>
    </div>
  );
}
```

### 6. Error Message with Space Reservation

```tsx
// Error message that doesn't shift layout
<div className="h-6 mt-1">
  {error && (
    <p className="text-sm text-red-500">{error}</p>
  )}
</div>

// For form field groups:
<div>
  <label>Field Name</label>
  <input {...props} />
  <div className="h-6 mt-1">
    {errors.fieldName && <p className="text-red-500">{errors.fieldName}</p>}
  </div>
</div>
```

### 7. Alert/Banner with Smooth Transition

```tsx
// Success/error alert that fades in smoothly
<div
  className={`transition-all duration-300 ${
    visible ? "opacity-100 h-auto mb-6" : "opacity-0 h-0 overflow-hidden"
  }`}
>
  {visible && (
    <div className="p-4 rounded-lg bg-green-50 border border-green-200">
      <p className="text-green-800">Success message</p>
    </div>
  )}
</div>
```

### 8. Flex Item with Stable Size

```tsx
// Image in flex container that won't shrink unexpectedly
<div className="flex gap-4">
  <div className="flex-shrink-0 w-24 h-24">
    <img
      src={imageUrl}
      alt="Image"
      width={96}
      height={96}
      className="w-full h-full object-cover"
    />
  </div>
  <div className="flex-1">
    <h3>Content</h3>
    <p>Description</p>
  </div>
</div>
```

### 9. Conditional Section with Reserved Space

```tsx
// Content that conditionally shows/hides without layout shift
<div
  className={`transition-all duration-300 overflow-hidden ${
    isExpanded ? "max-h-96 opacity-100" : "max-h-0 opacity-0"
  }`}
>
  <div className="p-4">Content inside</div>
</div>
```

### 10. Form with Validation Errors

```tsx
// Complete form field with proper error handling
<div className="space-y-4">
  <div>
    <label htmlFor="name" className="block text-sm font-semibold mb-2">
      Item Name <span className="text-red-500">*</span>
    </label>
    <input
      id="name"
      type="text"
      value={formData.name}
      onChange={(e) => updateField("name", e.target.value)}
      className={`w-full px-4 py-3 rounded-lg border transition-colors ${
        errors.name
          ? "border-red-500 focus:ring-red-500"
          : "border-gray-300 focus:ring-blue-500"
      } focus:ring-2 focus:outline-none`}
    />
    <div className="h-6 mt-1">
      {errors.name && <p className="text-sm text-red-500">{errors.name}</p>}
    </div>
  </div>
</div>
```

### 11. Image Gallery with Carousel

```tsx
// Gallery with fixed dimensions
<div className="space-y-4">
  {/* Main image with aspect ratio */}
  <div className="aspect-square w-full overflow-hidden rounded-lg bg-gray-100">
    <img
      src={selectedImage}
      alt="Product"
      width={500}
      height={500}
      className="w-full h-full object-cover"
    />
  </div>

  {/* Thumbnails */}
  <div className="flex gap-3 overflow-x-auto">
    {images.map((img, idx) => (
      <button
        key={idx}
        className="h-20 w-20 flex-shrink-0 overflow-hidden rounded"
        onClick={() => setSelected(img)}
      >
        <img
          src={img}
          alt={`Thumb ${idx}`}
          width={80}
          height={80}
          className="w-full h-full object-cover"
        />
      </button>
    ))}
  </div>
</div>
```

### 12. Card Component with Stable Layout

```tsx
// Card that doesn't shift when image loads
<div className="rounded-lg overflow-hidden shadow-lg">
  {/* Image with aspect ratio */}
  <div className="aspect-video w-full overflow-hidden bg-gray-200">
    <img
      src={imageUrl}
      alt="Card image"
      className="w-full h-full object-cover"
    />
  </div>

  {/* Content with fixed spacing */}
  <div className="p-6 space-y-4">
    <h3 className="text-lg font-bold">{title}</h3>
    <p className="text-gray-600">{description}</p>

    {/* Button with stable height */}
    <button className="w-full h-12 mt-4 bg-blue-600 text-white rounded-lg hover:bg-blue-700">
      Action
    </button>
  </div>
</div>
```

## Key Class Names

| Class                 | Purpose                     | Example           |
| --------------------- | --------------------------- | ----------------- |
| `flex-shrink-0`       | Prevent flex item shrinking | Image in flex row |
| `aspect-square`       | 1:1 aspect ratio            | Product images    |
| `aspect-video`        | 16:9 aspect ratio           | Hero images       |
| `h-0 overflow-hidden` | Hide while keeping 0 height | Hidden sections   |
| `transition-all`      | Smooth all property changes | Fade + height     |
| `duration-300`        | 300ms transition            | Smooth animations |
| `animate-pulse`       | Subtle loading animation    | Skeleton screens  |

## Testing Your Changes

```tsx
// Add this to components to test for CLS
useEffect(() => {
  if (typeof window !== "undefined" && "PerformanceObserver" in window) {
    const observer = new PerformanceObserver((list) => {
      for (const entry of list.getEntries()) {
        if (!(entry as any).hadRecentInput) {
          console.warn("[CLS Debug]", entry);
        }
      }
    });
    observer.observe({ type: "layout-shift", buffered: true });
    return () => observer.disconnect();
  }
}, []);
```

## Common Mistakes to Avoid

❌ **Bad**: Image without dimensions

```tsx
<img src="photo.jpg" alt="photo" />
```

✅ **Good**: Image with dimensions

```tsx
<img src="photo.jpg" alt="photo" width={400} height={300} />
```

---

❌ **Bad**: Error message appears/disappears

```tsx
<input />;
{
  error && <p>{error}</p>;
}
```

✅ **Good**: Error message space reserved

```tsx
<input />
<div className="h-6 mt-1">
  {error && <p>{error}</p>}
</div>
```

---

❌ **Bad**: Mismatched loading state

```tsx
if (loading) return <Spinner />;
return <DetailedContent />;
```

✅ **Good**: Skeleton matches content

```tsx
if (loading) return <DetailSkeleton />;
return <DetailedContent />;
```

---

❌ **Bad**: Flex item without shrink prevention

```tsx
<div className="flex gap-4">
  <img src="..." />
  <Content />
</div>
```

✅ **Good**: Prevent flex item shrinking

```tsx
<div className="flex gap-4">
  <img src="..." className="flex-shrink-0 w-16 h-16" />
  <Content />
</div>
```

## Summary

Use these patterns consistently across the project to maintain low CLS scores:

1. **Always use dimensions** for images
2. **Reserve space** for dynamic content
3. **Match loading states** to content layout
4. **Use smooth transitions** for conditional content
5. **Prevent flex shrinking** on critical items
6. **Monitor with console** warnings

For more details, see `CLS_OPTIMIZATION_GUIDE.md`
