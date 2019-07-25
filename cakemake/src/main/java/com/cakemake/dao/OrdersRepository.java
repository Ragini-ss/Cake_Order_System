package com.cakemake.dao;

import com.cakemake.model.Orders;
import org.springframework.data.repository.CrudRepository;

public interface OrdersRepository extends CrudRepository<Orders,Long> {
}
