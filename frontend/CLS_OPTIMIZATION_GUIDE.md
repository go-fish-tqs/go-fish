# Cumulative Layout Shift (CLS) - Optimization Guide

## Overview

This document outlines the CLS fixes applied to the GoFish frontend and provides guidelines for maintaining good CLS scores going forward.

## What is CLS?

Cumulative Layout Shift (CLS) measures the visual stability of a web page. It's one of Google's Core Web Vitals. A good CLS score is < 0.1, while > 0.25 is considered poor.

## Fixes Applied

### 1. Image Dimensions (Critical)

**Issue**: Images without explicit width/height cause layout shifts when they load
**Fixes Applied**:

- ✅ Added `width` and `height` attributes to all product images
- ✅ Added explicit dimensions to ItemCard images (400x192px)
- ✅ Added aspect-ratio constraints to gallery images (1:1 aspect ratio)
- ✅ Added dimensions to carousel background images (1200x480px)
- ✅ Added dimensions to thumbnail images (64x64px)

**Files Modified**:

- `app/items/_components/ItemCard.tsx`
- `app/items/[id]/_components/ItemGallery.tsx`
- `app/dashboard/components/FeaturedItemCarousel.tsx`

### 2. Loading State Skeleton Screens

**Issue**: Spinner loading states don't match content dimensions, causing shifts when real content loads
**Fixes Applied**:

- ✅ Replaced centered spinner with grid skeleton showing item cards
- ✅ Created matching skeleton for item detail page
- ✅ Added detailed skeleton for edit form page
- ✅ Reserved proper space in loading states

**Files Modified**:

- `app/items/_components/ItemsGrid.tsx`
- `app/items/[id]/page.tsx`
- `app/items/[id]/edit/ItemUpdateForm.tsx`

### 3. Error/Alert Messages

**Issue**: Error and success messages appear/disappear, shifting adjacent content
**Fixes Applied**:

- ✅ Used CSS transitions with `opacity` and `height` states
- ✅ Reserve space even when messages are hidden using `h-0 overflow-hidden`
- ✅ Added smooth transitions with `transition-all duration-300`
- ✅ Added `flex-shrink-0` to SVG icons to prevent sizing changes

**Files Modified**:

- `app/booking/add/page.tsx`
- `app/booking/_components/PaymentForm.tsx`

### 4. Form Validation Errors

**Issue**: Error text appears/disappears below form fields, shifting content below
**Fixes Applied**:

- ✅ Added `h-6` reserved height for error messages
- ✅ Empty `<div>` placeholder when no error exists
- ✅ Consistent line height prevents metric changes

**Files Modified**:

- `app/items/add/_components/NameField.tsx`
- `app/items/add/_components/DescriptionField.tsx`
- `app/items/add/_components/PriceField.tsx`

### 5. Container Space Reservation

**Issue**: Flexbox containers with `flex-shrink-0` prevent unintended size reduction
**Fixes Applied**:

- ✅ Added `flex-shrink-0` to image containers
- ✅ Explicit height or aspect-ratio on media containers

**Files Modified**:

- `app/booking/_components/ItemSummary.tsx`
- `app/items/_components/ItemCard.tsx`

### 6. Font Optimization

**Status**: Already optimized

- ✅ Using `next/font` which includes `font-display: swap`
- ✅ Fonts load asynchronously without blocking render
- ✅ System fonts as fallback prevents FOIT (Flash of Invisible Text)

**File**: `app/layout.tsx`

### 7. Monitoring & Debugging

**Added**: CLS monitoring utility

- ✅ Created `app/lib/cls-monitor.ts` for CLS measurement
- ✅ Created `app/components/CLSMonitor.tsx` to activate monitoring
- ✅ Tracks layout shifts and logs problematic elements
- ✅ Helps identify remaining CLS issues in development

## Best Practices for Future Development

### Images

```tsx
// ✅ GOOD - Explicit dimensions
<img src="..." alt="..." width={400} height={300} />

// ✅ GOOD - Aspect ratio container
<div className="aspect-video">
  <img src="..." alt="..." className="w-full h-full object-cover" />
</div>

// ❌ BAD - No dimensions
<img src="..." alt="..." />
```

### Loading States

```tsx
// ✅ GOOD - Match content dimensions
if (isLoading) {
  return (
    <div className="grid grid-cols-3 gap-4">
      {Array.from({ length: 6 }).map((_, i) => (
        <div key={i} className="aspect-square bg-gray-200 animate-pulse" />
      ))}
    </div>
  );
}

// ❌ BAD - Doesn't match content
if (isLoading) {
  return <div className="animate-spin">Loading...</div>;
}
```

### Form Errors

```tsx
// ✅ GOOD - Reserve space
<div className="h-6 mt-1">
  {error && <p className="text-red-500">{error}</p>}
</div>;

// ❌ BAD - Space appears/disappears
{
  error && <p className="mt-1 text-red-500">{error}</p>;
}
```

### Conditional Content

```tsx
// ✅ GOOD - Smooth transition
<div
  className={`transition-all duration-300 ${
    visible ? "opacity-100 h-auto" : "opacity-0 h-0 overflow-hidden"
  }`}
>
  {visible && <Content />}
</div>;

// ❌ BAD - Abrupt appearance
{
  visible && <Content />;
}
```

### Flexbox

```tsx
// ✅ GOOD - Prevent unexpected shrinking
<div className="flex-shrink-0 w-48 h-48">
  <img src="..." />
</div>

// ❌ BAD - Image might shrink unexpectedly
<div className="w-48 h-48">
  <img src="..." />
</div>
```

## Monitoring

### Development

The CLS monitoring utility automatically logs layout shifts in the console:

```
[CLS] Layout shift detected: 0.045
```

This helps identify issues before they affect users.

### Production

Use Lighthouse CI (already configured) to track CLS:

```bash
pnpm dlx @lhci/cli@0.13.x autorun
```

Check the generated reports in `.lighthouseci/` directory.

### Chrome DevTools

1. Open DevTools → Performance
2. Record a page session
3. Look for "Layout Shift" entries
4. Click to see which elements shifted

## Testing CLS

### Manual Testing

1. Open DevTools Console
2. Look for `[CLS]` warnings
3. Check each element that shifts
4. Fix by reserving space or using aspect ratios

### Automated Testing

- Lighthouse CI runs on every build
- Set CLS threshold in `lighthouserc.js` (currently unset, can add)
- Add to CI/CD pipeline for continuous monitoring

## Configuration

### lighthouserc.js

You can add CLS assertions:

```javascript
"cumulative-layout-shift": ["warn", { minScore: 0.9 }],
```

### Tailwind Classes Used

- `aspect-square` / `aspect-video` - Set aspect ratios
- `flex-shrink-0` - Prevent flex items from shrinking
- `h-0 overflow-hidden` - Hide while reserving zero height
- `transition-all` - Smooth opacity/height changes

## Resources

- [Web.dev - CLS Guide](https://web.dev/cls/)
- [Lighthouse Performance Guide](https://developers.google.com/web/tools/lighthouse/audits/cumulative-layout-shift)
- [Next.js Image Optimization](https://nextjs.org/docs/basic-features/image-optimization)

## Summary

All major CLS sources have been addressed:

- ✅ Images have explicit dimensions or aspect ratios
- ✅ Loading states match content layout
- ✅ Error messages reserve space
- ✅ Form validation doesn't shift layout
- ✅ Transitions are smooth with opacity
- ✅ Monitoring in place for future issues

Expected improvement: **CLS score should drop from ~0.15+ to < 0.1** (good range).
