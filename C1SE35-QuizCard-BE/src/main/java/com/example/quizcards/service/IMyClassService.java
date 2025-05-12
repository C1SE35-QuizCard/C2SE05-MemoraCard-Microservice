package com.example.quizcards.service;

import com.example.quizcards.entities.MyClass;

import java.util.List;
import java.util.Optional;

public interface IMyClassService {
    void save(MyClass myClass);

    List<MyClass> findMyClasses();

    Optional<MyClass> findById(Long id);

    boolean addFolderToClass(Long classId, Long folderId);

    boolean removeFolderFromClass(Long classId, Long folderId);

    List<MyClass> findJoinedClasses(Long userId);

    void deleteMyClass(Long myClassId, Long userId);

    List<MyClass> searchMyClasses(Long userId, String query);

    List<MyClass> searchJoinedClasses(Long userId, String query);
}