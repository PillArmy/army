package io.army.example.type.dao;

import io.army.example.type.domain.Postgre;

public interface PostgreDao extends TypeBaseDao {

    Postgre insert(Postgre postgre);

    Postgre update(Postgre postgre);

    Postgre findById(Long id);

    Postgre findFirst();

}
