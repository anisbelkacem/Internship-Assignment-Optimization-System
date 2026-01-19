import { useState } from 'react';
import '../styles/SearchFilter.css';

export interface FilterOption {
  label: string;
  value: string;
  field: string;
}

export interface FilterConfig {
  field: string;
  label: string;
  options: FilterOption[];
}

interface SearchFilterProps {
  searchPlaceholder: string;
  searchValue: string;
  onSearchChange: (value: string) => void;
  filters: FilterConfig[];
  activeFilters: Record<string, string>;
  onFilterChange: (field: string, value: string) => void;
  onClearFilters: () => void;
}

export default function SearchFilter({
  searchPlaceholder,
  searchValue,
  onSearchChange,
  filters,
  activeFilters,
  onFilterChange,
  onClearFilters,
}: SearchFilterProps) {
  const [showFilters, setShowFilters] = useState(false);

  const hasActiveFilters = Object.values(activeFilters).some(value => value !== '');
  const activeFilterCount = Object.values(activeFilters).filter(value => value !== '').length;

  return (
    <div className="search-filter-container">
      <div className="search-bar">
        <input
          type="text"
          className="search-input"
          placeholder={searchPlaceholder}
          value={searchValue}
          onChange={(e) => onSearchChange(e.target.value)}
        />
        <button
          className={`filter-toggle-btn ${hasActiveFilters ? 'has-filters' : ''}`}
          onClick={() => setShowFilters(!showFilters)}
        >
          🔍 Filter
          {activeFilterCount > 0 && (
            <span className="filter-count">{activeFilterCount}</span>
          )}
        </button>
      </div>

      {showFilters && (
        <div className="filters-panel">
          <div className="filters-header">
            <h3>Filter</h3>
            {hasActiveFilters && (
              <button className="clear-filters-btn" onClick={onClearFilters}>
                Zurücksetzen
              </button>
            )}
          </div>
          <div className="filters-grid">
            {filters.map((filter) => (
              <div key={filter.field} className="filter-group">
                <label className="filter-label">{filter.label}</label>
                <select
                  className="filter-select"
                  value={activeFilters[filter.field] || ''}
                  onChange={(e) => onFilterChange(filter.field, e.target.value)}
                >
                  <option value="">Alle</option>
                  {filter.options.map((option) => (
                    <option key={option.value} value={option.value}>
                      {option.label}
                    </option>
                  ))}
                </select>
              </div>
            ))}
          </div>
        </div>
      )}
    </div>
  );
}
