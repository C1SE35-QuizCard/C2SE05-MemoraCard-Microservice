package com.example.quizcards.service.impl;

import com.example.quizcards.dto.ICategorySetFlashcardDTO;
import com.example.quizcards.dto.ISetFlashcardDTO;
import com.example.quizcards.dto.request.CategorySetFlashcardAdminRequest;
import com.example.quizcards.exception.BadRequestException;
import com.example.quizcards.repository.ICategorySetFlashcardRepository;
import com.example.quizcards.service.ICategorySetFlashcardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.List;

@Service
public class CategorySetFlashcardServiceImpl implements ICategorySetFlashcardService {
    @Autowired
    private ICategorySetFlashcardRepository categoryRepository;

    private final int MAX_SIZE_PER_PAGE = 5;

    @Override
    public ICategorySetFlashcardDTO getCategorySetFlashcardById(Long categoryId) {
        return categoryRepository.findCategorySetFlashcardById(categoryId);
    }

    @Override
    public List<ICategorySetFlashcardDTO> getAll() {
        return categoryRepository.findAllCategorySetFlashcard();
    }

    @Override
    public List<ISetFlashcardDTO> findAllSetFlashcardsByCategoryId(Long categoryId) {
        return categoryRepository.findAllSetFlashcardsByCategoryId(categoryId);
    }

    @Override
    public List<ICategorySetFlashcardDTO> findTop1MostAccessedCategory() {
        return categoryRepository.findTopMostAccessedCategory().stream().limit(1).toList();
    }

    @Override
    public void addCategorySetFlashcard(String categoryName) {
        categoryRepository.createCategorySetFlashcard(categoryName);
    }

    @Override
    public void deleteCategorySetFlashcard(Long categoryId) {
        categoryRepository.deleteCategorySetFlashcard(categoryId);
    }

    @Override
    public void updateCategorySetFlashcard(CategorySetFlashcardAdminRequest request) {
        categoryRepository.updateCategorySetFlashcard(request.getCategoryId(), request.getCategoryName());
    }

    @Override
    public Page<ISetFlashcardDTO> findAllSetFlashcardsByCategoryId2(Long categoryId, int pages) {
        Pageable pageable = PageRequest.of(pages, MAX_SIZE_PER_PAGE);
        return categoryRepository.findAllSetFlashcardsByCategoryId2(categoryId, pageable);
    }

    @Override
    public Page<ICategorySetFlashcardDTO> getAllCategoryWithPagination(int pages, int size) {
        if (pages < 0) {
            throw new BadRequestException("Page must be greater than or equal to 0");
        }
        if (size < 1 || size > 100) {
            throw new BadRequestException("Size must be greater than 0 and less than or equal to 100");
        }
        Pageable pageable = PageRequest.of(pages, size);
        return categoryRepository.getAllByPagination(pageable);
    }
}
