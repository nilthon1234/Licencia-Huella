package com.licencia.persistence.repository;

import com.licencia.persistence.models.WinLin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IRepositoryWinLin extends JpaRepository<WinLin,Integer> {

    List<WinLin> findAll();

    boolean existsByWiniIncrip(String licenciaEnc);
}
