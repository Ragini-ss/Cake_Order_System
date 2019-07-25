package com.cakemake.dao;

import com.cakemake.model.Cart;
import org.springframework.data.repository.CrudRepository;

public interface CartRepository extends CrudRepository<Cart, Long> {


        }