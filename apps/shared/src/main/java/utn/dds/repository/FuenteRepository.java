package utn.dds.repository;

import utn.dds.dominio.fuentes.FuenteDeDatos;
import utn.dds.dominio.Hecho;
import java.util.List;

public abstract class FuenteRepository {
    protected IDAO<Hecho> dao;
    protected FuenteDeDatos fuenteDeDatos;

    public abstract List<Hecho> find();
    public abstract void save(List<Hecho> hechos);
} 