import React, { useState, useEffect, useMemo } from 'react';
import { 
  FolderPlus, 
  Edit3, 
  Trash2, 
  CheckCircle, 
  XCircle, 
  Search, 
  Filter, 
  Image as ImageIcon, 
  Layers, 
  AlertTriangle, 
  ArrowUpDown, 
  RefreshCw, 
  Eye, 
  EyeOff, 
  ExternalLink,
  Plus,
  Package,
  Sparkles,
  HelpCircle
} from 'lucide-react';
import { DEFAULT_CATEGORIES } from '../data/storeData';
import { 
  fetchCategoriesFromFirestore, 
  saveAdminCategoryToFirestore, 
  toggleAdminCategoryActive, 
  deleteAdminCategoryFromFirestore, 
  seedDefaultCategoriesToFirestore,
  fetchAdminProducts 
} from '../firebase';

// High-quality curated category image presets for mobile store
const PRESET_CATEGORY_IMAGES = [
  { name: 'Smartphones', url: 'https://images.unsplash.com/photo-1592899677977-9c10ca588bbd?w=500&auto=format&fit=crop&q=80' },
  { name: 'Pre-Owned Phones', url: 'https://images.unsplash.com/photo-1510557880182-3d4d3cba35a5?w=500&auto=format&fit=crop&q=80' },
  { name: 'Fast Chargers', url: 'https://images.unsplash.com/photo-1583863788434-e58a36330cf0?w=500&auto=format&fit=crop&q=80' },
  { name: 'Data Cables', url: 'https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c?w=500&auto=format&fit=crop&q=80' },
  { name: 'Audio & Neckband', url: 'https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=500&auto=format&fit=crop&q=80' },
  { name: 'Cases & Covers', url: 'https://images.unsplash.com/photo-1584438784894-089d6a62b8fa?w=500&auto=format&fit=crop&q=80' },
  { name: 'Repair & Tools', url: 'https://images.unsplash.com/photo-1581092160607-ee22621dd758?w=500&auto=format&fit=crop&q=80' },
  { name: 'Wireless Bluetooth', url: 'https://images.unsplash.com/photo-1608043152269-423dbba4e7e1?w=500&auto=format&fit=crop&q=80' }
];

export default function CategoryManagementTab({ currentUser, onShowToast }) {
  const [categories, setCategories] = useState([]);
  const [products, setProducts] = useState([]);
  const [isLoading, setIsLoading] = useState(true);
  const [isSaving, setIsSaving] = useState(false);
  const [searchQuery, setSearchQuery] = useState('');
  const [statusFilter, setStatusFilter] = useState('ALL'); // 'ALL' | 'ACTIVE' | 'INACTIVE'

  // Modal states
  const [isFormOpen, setIsFormOpen] = useState(false);
  const [editingCategory, setEditingCategory] = useState(null);
  const [formData, setFormData] = useState({
    name: '',
    slug: '',
    imageUrl: '',
    description: '',
    isActive: true,
    displayOrder: 1
  });

  // Delete modal state
  const [categoryToDelete, setCategoryToDelete] = useState(null);
  const [reassignCategorySlug, setReassignCategorySlug] = useState('');
  const [isDeleting, setIsDeleting] = useState(false);

  useEffect(() => {
    loadData();
  }, []);

  const loadData = async () => {
    setIsLoading(true);
    try {
      const [catRes, prodRes] = await Promise.all([
        fetchCategoriesFromFirestore(),
        fetchAdminProducts()
      ]);

      let catList = [];
      if (catRes.success && catRes.data && catRes.data.length > 0) {
        catList = catRes.data;
      } else {
        // Fallback to default categories
        catList = DEFAULT_CATEGORIES;
      }
      setCategories(catList);

      if (prodRes.success && prodRes.data) {
        setProducts(prodRes.data);
      }
    } catch (err) {
      console.error("Error loading categories or products:", err);
      onShowToast?.("Error fetching categories", "error");
    } finally {
      setIsLoading(false);
    }
  };

  // Seed default categories into Firestore if empty
  const handleSeedDefaults = async () => {
    setIsSaving(true);
    try {
      await seedDefaultCategoriesToFirestore(currentUser, DEFAULT_CATEGORIES);
      onShowToast?.("Default categories successfully populated into Firestore!", "success");
      await loadData();
    } catch (err) {
      onShowToast?.(err.message || "Failed to seed categories", "error");
    } finally {
      setIsSaving(false);
    }
  };

  // Compute products assigned to each category
  const productCountsBySlug = useMemo(() => {
    const counts = {};
    products.forEach(p => {
      const cat = (p.category || '').toLowerCase().trim();
      counts[cat] = (counts[cat] || 0) + 1;
    });
    return counts;
  }, [products]);

  const getProductCountForCategory = (cat) => {
    const slug = (cat.slug || '').toLowerCase();
    const id = (cat.id || '').toLowerCase();
    // Count matches by slug or direct ID
    let count = productCountsBySlug[slug] || 0;
    if (id !== slug && productCountsBySlug[id]) {
      count += productCountsBySlug[id];
    }
    // Also match legacy keywords for mobile & accessories
    if (slug === 'smartphones') {
      count += (productCountsBySlug['mobile'] || 0) + (productCountsBySlug['phone'] || 0);
    }
    return count;
  };

  // Filtered categories
  const filteredCategories = useMemo(() => {
    return categories.filter(cat => {
      const matchesSearch = 
        (cat.name || '').toLowerCase().includes(searchQuery.toLowerCase()) ||
        (cat.slug || '').toLowerCase().includes(searchQuery.toLowerCase()) ||
        (cat.description || '').toLowerCase().includes(searchQuery.toLowerCase());

      const matchesStatus = 
        statusFilter === 'ALL' ||
        (statusFilter === 'ACTIVE' && cat.isActive) ||
        (statusFilter === 'INACTIVE' && !cat.isActive);

      return matchesSearch && matchesStatus;
    }).sort((a, b) => (a.displayOrder || 99) - (b.displayOrder || 99));
  }, [categories, searchQuery, statusFilter]);

  // Open Form for Add
  const handleOpenAddForm = () => {
    setEditingCategory(null);
    const nextOrder = categories.length > 0 
      ? Math.max(...categories.map(c => c.displayOrder || 0)) + 1 
      : 1;
    setFormData({
      name: '',
      slug: '',
      imageUrl: PRESET_CATEGORY_IMAGES[0].url,
      description: '',
      isActive: true,
      displayOrder: nextOrder
    });
    setIsFormOpen(true);
  };

  // Open Form for Edit
  const handleOpenEditForm = (cat) => {
    setEditingCategory(cat);
    setFormData({
      name: cat.name || '',
      slug: cat.slug || '',
      imageUrl: cat.imageUrl || '',
      description: cat.description || '',
      isActive: cat.isActive !== undefined ? cat.isActive : true,
      displayOrder: cat.displayOrder || 1
    });
    setIsFormOpen(true);
  };

  // Auto-generate slug when name changes
  const handleNameChange = (e) => {
    const newName = e.target.value;
    if (!editingCategory) {
      const generatedSlug = newName
        .toLowerCase()
        .replace(/[^a-z0-9]+/g, '-')
        .replace(/(^-|-$)/g, '');
      setFormData(prev => ({ ...prev, name: newName, slug: generatedSlug }));
    } else {
      setFormData(prev => ({ ...prev, name: newName }));
    }
  };

  // Save Category (Create or Edit)
  const handleSaveCategory = async (e) => {
    e.preventDefault();
    if (!formData.name.trim()) {
      onShowToast?.("Category name is required.", "error");
      return;
    }

    setIsSaving(true);
    try {
      const payload = {
        id: editingCategory ? editingCategory.id : (formData.slug ? `cat-${formData.slug}` : `cat-${Date.now()}`),
        name: formData.name.trim(),
        slug: (formData.slug || formData.name.toLowerCase().replace(/[^a-z0-9]+/g, '-')).trim(),
        imageUrl: formData.imageUrl.trim(),
        description: formData.description.trim(),
        isActive: Boolean(formData.isActive),
        displayOrder: parseInt(formData.displayOrder, 10) || 1
      };

      await saveAdminCategoryToFirestore(payload, currentUser);
      onShowToast?.(
        editingCategory 
          ? `Category '${payload.name}' updated successfully!` 
          : `Category '${payload.name}' created successfully!`,
        "success"
      );

      setIsFormOpen(false);
      setEditingCategory(null);
      await loadData();
    } catch (err) {
      console.error("Save category error:", err);
      onShowToast?.(err.message || "Failed to save category.", "error");
    } finally {
      setIsSaving(false);
    }
  };

  // Toggle Active/Inactive
  const handleToggleActive = async (cat) => {
    try {
      await toggleAdminCategoryActive(cat.id, cat.isActive, currentUser);
      setCategories(prev => prev.map(item => 
        item.id === cat.id ? { ...item, isActive: !item.isActive } : item
      ));
      onShowToast?.(`Category '${cat.name}' is now ${!cat.isActive ? 'Active' : 'Disabled'}.`, "info");
    } catch (err) {
      onShowToast?.(err.message || "Failed to toggle category status.", "error");
    }
  };

  // Prepare Delete
  const handlePrepareDelete = (cat) => {
    const assignedCount = getProductCountForCategory(cat);
    // Find candidate categories for reassignment (excluding the one being deleted)
    const otherCats = categories.filter(c => c.id !== cat.id && c.slug !== cat.slug && c.isActive);
    const defaultReassign = otherCats.length > 0 ? otherCats[0].slug : '';
    setReassignCategorySlug(defaultReassign);
    setCategoryToDelete({ ...cat, assignedCount });
  };

  // Confirm Delete
  const handleConfirmDelete = async () => {
    if (!categoryToDelete) return;
    setIsDeleting(true);
    try {
      const reassign = categoryToDelete.assignedCount > 0 ? reassignCategorySlug : null;
      await deleteAdminCategoryFromFirestore(
        categoryToDelete.id, 
        categoryToDelete.slug, 
        reassign, 
        currentUser
      );

      onShowToast?.(
        categoryToDelete.assignedCount > 0
          ? `Category '${categoryToDelete.name}' deleted. ${categoryToDelete.assignedCount} products reassigned to '${reassignCategorySlug}'.`
          : `Category '${categoryToDelete.name}' deleted successfully.`,
        "success"
      );

      setCategoryToDelete(null);
      await loadData();
    } catch (err) {
      console.error("Delete category error:", err);
      onShowToast?.(err.message || "Failed to delete category.", "error");
    } finally {
      setIsDeleting(false);
    }
  };

  const totalActive = categories.filter(c => c.isActive).length;
  const totalInactive = categories.filter(c => !c.isActive).length;

  return (
    <div className="space-y-6">
      {/* Category Stats Header */}
      <div className="grid grid-cols-2 sm:grid-cols-4 gap-3.5">
        <div className="p-4 rounded-xl bg-[#141414] border border-[#2E2E2E]">
          <div className="flex items-center justify-between text-gray-400 mb-1">
            <span className="text-xs uppercase font-bold tracking-wider">Total Categories</span>
            <Layers size={16} className="text-[#D4AF37]" />
          </div>
          <div className="text-2xl font-black text-white">{categories.length}</div>
          <span className="text-[10px] text-gray-500">Taxonomy items</span>
        </div>

        <div className="p-4 rounded-xl bg-[#141414] border border-[#2E2E2E]">
          <div className="flex items-center justify-between text-gray-400 mb-1">
            <span className="text-xs uppercase font-bold tracking-wider">Active Categories</span>
            <CheckCircle size={16} className="text-emerald-400" />
          </div>
          <div className="text-2xl font-black text-emerald-400">{totalActive}</div>
          <span className="text-[10px] text-gray-500">Visible to customers</span>
        </div>

        <div className="p-4 rounded-xl bg-[#141414] border border-[#2E2E2E]">
          <div className="flex items-center justify-between text-gray-400 mb-1">
            <span className="text-xs uppercase font-bold tracking-wider">Disabled</span>
            <XCircle size={16} className="text-red-400" />
          </div>
          <div className="text-2xl font-black text-red-400">{totalInactive}</div>
          <span className="text-[10px] text-gray-500">Hidden from front store</span>
        </div>

        <div className="p-4 rounded-xl bg-[#141414] border border-[#2E2E2E]">
          <div className="flex items-center justify-between text-gray-400 mb-1">
            <span className="text-xs uppercase font-bold tracking-wider">Total Products</span>
            <Package size={16} className="text-purple-400" />
          </div>
          <div className="text-2xl font-black text-purple-400">{products.length}</div>
          <span className="text-[10px] text-gray-500">Categorized catalog items</span>
        </div>
      </div>

      {/* Action Bar: Search, Filter, Add Category & Seed Defaults */}
      <div className="flex flex-col md:flex-row md:items-center justify-between gap-3 bg-[#141414] border border-[#262626] p-4 rounded-xl">
        <div className="flex flex-1 flex-col sm:flex-row items-center gap-3">
          {/* Search */}
          <div className="relative w-full sm:w-72">
            <Search size={16} className="absolute left-3.5 top-1/2 -translate-y-1/2 text-gray-500" />
            <input
              type="text"
              placeholder="Search category name, slug..."
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              className="w-full bg-[#1C1C1C] border border-[#333333] rounded-lg pl-10 pr-3 py-2 text-xs text-white placeholder-gray-500 focus:outline-none focus:border-[#D4AF37]"
            />
          </div>

          {/* Status Filter */}
          <div className="flex items-center gap-1.5 w-full sm:w-auto">
            <Filter size={15} className="text-gray-400 ml-1" />
            <select
              value={statusFilter}
              onChange={(e) => setStatusFilter(e.target.value)}
              className="bg-[#1C1C1C] border border-[#333333] rounded-lg px-3 py-2 text-xs text-gray-300 focus:outline-none focus:border-[#D4AF37]"
            >
              <option value="ALL">All Statuses ({categories.length})</option>
              <option value="ACTIVE">Active Only ({totalActive})</option>
              <option value="INACTIVE">Hidden / Inactive ({totalInactive})</option>
            </select>
          </div>
        </div>

        {/* Action Buttons */}
        <div className="flex items-center gap-2">
          <button
            onClick={loadData}
            disabled={isLoading}
            className="py-2 px-3 rounded-lg bg-[#222222] hover:bg-[#2A2A2A] text-xs font-semibold text-gray-300 hover:text-white transition flex items-center gap-1.5"
            title="Refresh categories"
          >
            <RefreshCw size={14} className={isLoading ? 'animate-spin text-[#D4AF37]' : ''} />
            <span className="hidden sm:inline">Refresh</span>
          </button>

          {categories.length === 0 && (
            <button
              onClick={handleSeedDefaults}
              disabled={isSaving}
              className="py-2 px-3 rounded-lg bg-[#D4AF37]/20 border border-[#D4AF37]/50 text-xs font-semibold text-[#D4AF37] hover:bg-[#D4AF37]/30 transition flex items-center gap-1.5"
            >
              <Sparkles size={14} />
              <span>Seed Defaults</span>
            </button>
          )}

          <button
            onClick={handleOpenAddForm}
            className="py-2 px-4 rounded-lg bg-gradient-to-r from-[#D4AF37] to-[#B8860B] text-black font-bold text-xs hover:brightness-110 transition shadow-lg flex items-center gap-1.5"
          >
            <Plus size={16} />
            <span>Add New Category</span>
          </button>
        </div>
      </div>

      {/* Categories Cards / Table */}
      {isLoading ? (
        <div className="p-12 text-center bg-[#141414] rounded-2xl border border-[#222222]">
          <RefreshCw size={28} className="animate-spin text-[#D4AF37] mx-auto mb-3" />
          <p className="text-sm text-gray-400">Loading store categories from Firestore...</p>
        </div>
      ) : filteredCategories.length === 0 ? (
        <div className="p-12 text-center bg-[#141414] rounded-2xl border border-[#222222]">
          <Layers size={36} className="text-gray-600 mx-auto mb-3" />
          <h3 className="text-base font-bold text-white mb-1">No categories found</h3>
          <p className="text-xs text-gray-400 mb-4">
            {searchQuery || statusFilter !== 'ALL'
              ? 'Try adjusting your search query or status filter.'
              : 'Add your first product category or seed the store defaults.'}
          </p>
          <button
            onClick={handleOpenAddForm}
            className="py-2 px-4 rounded-lg bg-[#D4AF37] text-black font-bold text-xs"
          >
            Add New Category
          </button>
        </div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
          {filteredCategories.map((cat) => {
            const productCount = getProductCountForCategory(cat);
            return (
              <div
                key={cat.id}
                className={`bg-[#141414] rounded-xl border p-4.5 transition flex flex-col justify-between ${
                  cat.isActive
                    ? 'border-[#262626] hover:border-[#D4AF37]/50'
                    : 'border-red-950/40 bg-[#120F0F] opacity-80'
                }`}
              >
                <div>
                  {/* Category Card Header */}
                  <div className="flex items-start gap-3.5 mb-3.5">
                    {/* Thumbnail Image */}
                    <div className="w-16 h-16 rounded-lg bg-[#1F1F1F] border border-[#333333] overflow-hidden flex-shrink-0 relative group">
                      {cat.imageUrl ? (
                        <img
                          src={cat.imageUrl}
                          alt={cat.name}
                          className="w-full h-full object-cover group-hover:scale-105 transition"
                          onError={(e) => {
                            e.target.onerror = null;
                            e.target.src = PRESET_CATEGORY_IMAGES[0].url;
                          }}
                        />
                      ) : (
                        <div className="w-full h-full flex items-center justify-center text-gray-500">
                          <Layers size={22} />
                        </div>
                      )}
                      <span className="absolute bottom-0 right-0 bg-black/80 px-1 py-0.5 text-[9px] font-mono text-[#D4AF37] rounded-tl">
                        #{cat.displayOrder || 1}
                      </span>
                    </div>

                    {/* Title & Slug */}
                    <div className="flex-1 min-w-0">
                      <div className="flex items-center justify-between gap-1 mb-1">
                        <h4 className="text-sm font-bold text-white truncate">{cat.name}</h4>
                        <span
                          className={`text-[10px] font-bold px-2 py-0.5 rounded-full inline-flex items-center gap-1 ${
                            cat.isActive
                              ? 'bg-emerald-500/10 text-emerald-400 border border-emerald-500/30'
                              : 'bg-red-500/10 text-red-400 border border-red-500/30'
                          }`}
                        >
                          {cat.isActive ? <Eye size={10} /> : <EyeOff size={10} />}
                          <span>{cat.isActive ? 'Active' : 'Hidden'}</span>
                        </span>
                      </div>

                      <div className="text-[11px] font-mono text-gray-400 mb-1">
                        slug: <span className="text-[#D4AF37]">{cat.slug}</span>
                      </div>

                      <div className="inline-flex items-center gap-1.5 text-[11px] font-semibold text-gray-300">
                        <Package size={12} className="text-gray-400" />
                        <span>{productCount} {productCount === 1 ? 'product' : 'products'} assigned</span>
                      </div>
                    </div>
                  </div>

                  {/* Description */}
                  {cat.description && (
                    <p className="text-xs text-gray-400 line-clamp-2 mb-3 bg-[#1A1A1A] p-2.5 rounded-lg border border-[#292929]">
                      {cat.description}
                    </p>
                  )}
                </div>

                {/* Footer Controls: Order, Toggle Switch, Edit & Delete */}
                <div className="pt-3 border-t border-[#262626] flex items-center justify-between gap-2 mt-2">
                  <div className="flex items-center gap-2">
                    <span className="text-[11px] text-gray-400">Status:</span>
                    <button
                      onClick={() => handleToggleActive(cat)}
                      className={`relative inline-flex h-5 w-9 items-center rounded-full transition-colors focus:outline-none ${
                        cat.isActive ? 'bg-emerald-500' : 'bg-gray-700'
                      }`}
                      title={cat.isActive ? 'Click to hide category from customers' : 'Click to activate category'}
                    >
                      <span
                        className={`inline-block h-3.5 w-3.5 transform rounded-full bg-white transition-transform ${
                          cat.isActive ? 'translate-x-4.5' : 'translate-x-1'
                        }`}
                      />
                    </button>
                  </div>

                  <div className="flex items-center gap-1.5">
                    <button
                      onClick={() => handleOpenEditForm(cat)}
                      className="p-1.5 rounded-lg bg-[#222222] hover:bg-[#2A2A2A] text-gray-300 hover:text-white border border-[#333333] transition"
                      title="Edit Category details"
                    >
                      <Edit3 size={14} />
                    </button>

                    <button
                      onClick={() => handlePrepareDelete(cat)}
                      className="p-1.5 rounded-lg bg-red-500/10 hover:bg-red-500/20 text-red-400 border border-red-500/30 transition"
                      title="Delete category safely"
                    >
                      <Trash2 size={14} />
                    </button>
                  </div>
                </div>
              </div>
            );
          })}
        </div>
      )}

      {/* ========================================================= */}
      {/* ADD / EDIT CATEGORY MODAL */}
      {/* ========================================================= */}
      {isFormOpen && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/80 backdrop-blur-sm">
          <div className="bg-[#141414] border border-[#2E2E2E] rounded-2xl max-w-xl w-full p-6 shadow-2xl space-y-5 max-h-[90vh] overflow-y-auto">
            {/* Modal Header */}
            <div className="flex items-center justify-between pb-3 border-b border-[#262626]">
              <div className="flex items-center gap-2">
                <div className="p-2 rounded-lg bg-[#D4AF37]/10 text-[#D4AF37] border border-[#D4AF37]/30">
                  <Layers size={18} />
                </div>
                <div>
                  <h3 className="text-base font-bold text-white">
                    {editingCategory ? 'Edit Store Category' : 'Add New Category'}
                  </h3>
                  <p className="text-xs text-gray-400">
                    Configure store category details and display ordering
                  </p>
                </div>
              </div>
              <button
                onClick={() => setIsFormOpen(false)}
                className="text-gray-400 hover:text-white p-1 text-sm font-bold"
              >
                ✕
              </button>
            </div>

            <form onSubmit={handleSaveCategory} className="space-y-4">
              {/* Category Name */}
              <div>
                <label className="block text-xs font-semibold text-gray-300 mb-1.5">
                  Category Name <span className="text-[#D4AF37]">*</span>
                </label>
                <input
                  type="text"
                  required
                  placeholder="e.g. Smart Watches & Wearables"
                  value={formData.name}
                  onChange={handleNameChange}
                  className="w-full bg-[#1C1C1C] border border-[#333333] rounded-lg px-3.5 py-2.5 text-xs text-white placeholder-gray-500 focus:outline-none focus:border-[#D4AF37]"
                />
              </div>

              {/* Slug & Display Order in 2-cols */}
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-3.5">
                <div>
                  <label className="block text-xs font-semibold text-gray-300 mb-1.5">
                    Category Slug / ID <span className="text-[#D4AF37]">*</span>
                  </label>
                  <input
                    type="text"
                    required
                    placeholder="e.g. smart-watches"
                    value={formData.slug}
                    onChange={(e) => setFormData({ ...formData, slug: e.target.value.toLowerCase().replace(/\s+/g, '-') })}
                    className="w-full bg-[#1C1C1C] border border-[#333333] rounded-lg px-3.5 py-2.5 text-xs text-white font-mono placeholder-gray-500 focus:outline-none focus:border-[#D4AF37]"
                  />
                  <span className="text-[10px] text-gray-500">Used for URL filtering and product matching</span>
                </div>

                <div>
                  <label className="block text-xs font-semibold text-gray-300 mb-1.5">
                    Display Order Index <span className="text-[#D4AF37]">*</span>
                  </label>
                  <input
                    type="number"
                    min="1"
                    max="999"
                    required
                    value={formData.displayOrder}
                    onChange={(e) => setFormData({ ...formData, displayOrder: e.target.value })}
                    className="w-full bg-[#1C1C1C] border border-[#333333] rounded-lg px-3.5 py-2.5 text-xs text-white focus:outline-none focus:border-[#D4AF37]"
                  />
                  <span className="text-[10px] text-gray-500">Lower numbers appear first on frontend navigation</span>
                </div>
              </div>

              {/* Category Image URL */}
              <div>
                <label className="block text-xs font-semibold text-gray-300 mb-1.5">
                  Category Image URL
                </label>
                <div className="flex gap-2">
                  <input
                    type="url"
                    placeholder="https://images.unsplash.com/..."
                    value={formData.imageUrl}
                    onChange={(e) => setFormData({ ...formData, imageUrl: e.target.value })}
                    className="flex-1 bg-[#1C1C1C] border border-[#333333] rounded-lg px-3.5 py-2 text-xs text-white placeholder-gray-500 focus:outline-none focus:border-[#D4AF37]"
                  />
                  {formData.imageUrl && (
                    <div className="w-9 h-9 rounded-lg border border-[#333333] overflow-hidden flex-shrink-0">
                      <img src={formData.imageUrl} alt="Preview" className="w-full h-full object-cover" />
                    </div>
                  )}
                </div>

                {/* Quick Presets */}
                <div className="mt-2">
                  <span className="text-[10px] font-semibold text-gray-400 block mb-1">
                    Or select a quick image preset:
                  </span>
                  <div className="grid grid-cols-4 gap-2">
                    {PRESET_CATEGORY_IMAGES.map((preset, idx) => (
                      <button
                        key={idx}
                        type="button"
                        onClick={() => setFormData({ ...formData, imageUrl: preset.url })}
                        className={`p-1.5 rounded-lg border text-left flex items-center gap-1.5 transition ${
                          formData.imageUrl === preset.url
                            ? 'border-[#D4AF37] bg-[#D4AF37]/10'
                            : 'border-[#262626] bg-[#1A1A1A] hover:border-[#383838]'
                        }`}
                      >
                        <img src={preset.url} alt={preset.name} className="w-5 h-5 rounded object-cover" />
                        <span className="text-[10px] text-gray-300 truncate">{preset.name}</span>
                      </button>
                    ))}
                  </div>
                </div>
              </div>

              {/* Description */}
              <div>
                <label className="block text-xs font-semibold text-gray-300 mb-1.5">
                  Category Description (Optional)
                </label>
                <textarea
                  rows="2"
                  placeholder="Short description shown to customers on category headers..."
                  value={formData.description}
                  onChange={(e) => setFormData({ ...formData, description: e.target.value })}
                  className="w-full bg-[#1C1C1C] border border-[#333333] rounded-lg p-3 text-xs text-white placeholder-gray-500 focus:outline-none focus:border-[#D4AF37]"
                />
              </div>

              {/* Status Toggle */}
              <div className="flex items-center justify-between p-3 rounded-lg bg-[#1A1A1A] border border-[#2E2E2E]">
                <div>
                  <span className="text-xs font-semibold text-white block">Category Status</span>
                  <span className="text-[10px] text-gray-400">
                    {formData.isActive ? 'Active and visible to customers on website and app' : 'Hidden from customers'}
                  </span>
                </div>
                <button
                  type="button"
                  onClick={() => setFormData({ ...formData, isActive: !formData.isActive })}
                  className={`relative inline-flex h-6 w-11 items-center rounded-full transition-colors ${
                    formData.isActive ? 'bg-emerald-500' : 'bg-gray-700'
                  }`}
                >
                  <span
                    className={`inline-block h-4 w-4 transform rounded-full bg-white transition-transform ${
                      formData.isActive ? 'translate-x-6' : 'translate-x-1'
                    }`}
                  />
                </button>
              </div>

              {/* Modal Buttons */}
              <div className="flex items-center justify-end gap-2.5 pt-3 border-t border-[#262626]">
                <button
                  type="button"
                  onClick={() => setIsFormOpen(false)}
                  className="py-2.5 px-4 rounded-xl border border-[#333333] hover:border-gray-500 text-xs font-semibold text-gray-300 transition"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  disabled={isSaving}
                  className="py-2.5 px-5 rounded-xl bg-gradient-to-r from-[#D4AF37] to-[#B8860B] text-black font-bold text-xs hover:brightness-110 transition flex items-center gap-2 shadow-lg"
                >
                  {isSaving && <RefreshCw size={14} className="animate-spin" />}
                  <span>Save Category</span>
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* ========================================================= */}
      {/* SAFE DELETE CATEGORY MODAL */}
      {/* ========================================================= */}
      {categoryToDelete && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/80 backdrop-blur-sm">
          <div className="bg-[#141414] border border-red-500/40 rounded-2xl max-w-md w-full p-6 shadow-2xl space-y-4">
            <div className="flex items-center gap-3">
              <div className="p-2.5 rounded-xl bg-red-500/20 text-red-400 border border-red-500/30">
                <AlertTriangle size={24} />
              </div>
              <div>
                <h3 className="text-base font-bold text-white">Delete Category?</h3>
                <p className="text-xs text-gray-400">
                  Category: <strong className="text-white">{categoryToDelete.name}</strong>
                </p>
              </div>
            </div>

            {categoryToDelete.assignedCount > 0 ? (
              <div className="space-y-3 bg-amber-500/10 border border-amber-500/30 rounded-xl p-3.5">
                <div className="flex items-start gap-2 text-amber-400 text-xs font-semibold">
                  <AlertTriangle size={16} className="flex-shrink-0 mt-0.5" />
                  <span>
                    Warning: <strong>{categoryToDelete.assignedCount}</strong> products currently belong to this category!
                  </span>
                </div>
                <p className="text-[11px] text-gray-300 leading-relaxed">
                  Products will <strong className="text-white underline">never</strong> be deleted automatically. 
                  Please select an active destination category to reassign these products safely:
                </p>

                <div>
                  <label className="block text-[11px] font-bold text-gray-300 mb-1">
                    Reassign products to:
                  </label>
                  <select
                    value={reassignCategorySlug}
                    onChange={(e) => setReassignCategorySlug(e.target.value)}
                    className="w-full bg-[#1C1C1C] border border-[#444444] rounded-lg p-2 text-xs text-white focus:outline-none focus:border-[#D4AF37]"
                  >
                    {categories
                      .filter(c => c.id !== categoryToDelete.id && c.slug !== categoryToDelete.slug)
                      .map(c => (
                        <option key={c.id} value={c.slug}>
                          {c.name} ({c.slug})
                        </option>
                      ))}
                  </select>
                </div>
              </div>
            ) : (
              <p className="text-xs text-gray-300 leading-relaxed bg-[#1A1A1A] p-3 rounded-lg border border-[#2E2E2E]">
                This category has <strong>0 products</strong> assigned. Are you sure you want to permanently remove category <strong>'{categoryToDelete.name}'</strong>?
              </p>
            )}

            <div className="flex items-center justify-end gap-2.5 pt-2">
              <button
                type="button"
                onClick={() => setCategoryToDelete(null)}
                className="py-2 px-4 rounded-xl border border-[#333333] hover:border-gray-500 text-xs font-semibold text-gray-300 transition"
              >
                Cancel
              </button>
              <button
                type="button"
                disabled={isDeleting || (categoryToDelete.assignedCount > 0 && !reassignCategorySlug)}
                onClick={handleConfirmDelete}
                className="py-2 px-4 rounded-xl bg-red-600 hover:bg-red-700 text-white font-bold text-xs transition flex items-center gap-1.5 shadow-lg"
              >
                {isDeleting && <RefreshCw size={14} className="animate-spin" />}
                <span>
                  {categoryToDelete.assignedCount > 0 ? 'Reassign & Delete' : 'Confirm Delete'}
                </span>
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
