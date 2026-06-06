package io.army.example.type.service;

import io.army.example.type.domain.Postgre;

public interface PostgreService extends TypeBaseService {

    Postgre insert(Postgre postgre);

    Postgre update(Postgre postgre);

    Postgre findById(Long id);

    Postgre findFirst();

}
