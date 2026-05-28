package com.mavic.backend.controller;

import com.mavic.backend.dto.NewMenuItemDto;
import com.mavic.backend.model.Menuitem;
import com.mavic.backend.model.Restaurant;
import com.mavic.backend.model.enums.Category;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface RestaurantMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "restaurant", source = "restaurant")
    @Mapping(target = "name", source = "newMenuItemDto.name")
    @Mapping(target = "description", source = "newMenuItemDto.description")
    @Mapping(target = "price", source = "newMenuItemDto.price")
    @Mapping(target = "category", source = "newMenuItemDto.category")
    @Mapping(target = "isAvailable", constant = "true")
    @Mapping(target = "orderitems", ignore = true)
    Menuitem toMenuItem(NewMenuItemDto newMenuItemDto, Restaurant restaurant);
    
    default Category mapCategory(String category) {
        return category != null ? Category.valueOf(category) : null;
    }
}
