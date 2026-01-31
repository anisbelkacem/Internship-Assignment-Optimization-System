# Project Report

This directory contains the LaTeX source files for the project report.

## Building the Report

### Prerequisites
- LaTeX distribution (TeX Live, MiKTeX, or MacTeX)
- PDF viewer

### Compilation

To compile the report, run:

```bash
pdflatex main.tex
bibtex main
pdflatex main.tex
pdflatex main.tex
```

Or use a LaTeX editor like TeXstudio, Overleaf, or VS Code with LaTeX Workshop extension.

### Quick Build (VS Code)

If you have the LaTeX Workshop extension installed:
1. Open `main.tex`
2. Press `Ctrl+Alt+B` (Windows/Linux) or `Cmd+Option+B` (Mac)

## Structure

- `main.tex` - Main document file
- `references.bib` - Bibliography references
- `figures/` - Directory for images and diagrams (create as needed)
