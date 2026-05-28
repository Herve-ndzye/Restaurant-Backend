package com.mavic.backend.controller;

import com.mavic.backend.dto.NewMenuItemDto;
import com.mavic.backend.model.Menuitem;
import com.mavic.backend.model.Restaurant;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-05-28T15:43:23+0200",
    comments = "version: 1.6.3, compiler: javac, environment: Java 24.0.2 (Oracle Corporation)"
)
@Component
public class RestaurantMapperImpl implements RestaurantMapper {

    @Override
    public Menuitem toMenuItem(NewMenuItemDto newMenuItemDto, Restaurant restaurant) {
        if ( newMenuItemDto == null && restaurant == null ) {
            return null;
        }

        Menuitem menuitem = new Menuitem();

        if ( newMenuItemDto != null ) {
            menuitem.setName( newMenuItemDto.getName() );
            menuitem.setDescription( newMenuItemDto.getDescription() );
            menuitem.setPrice( newMenuItemDto.getPrice() );
            menuitem.setCategory( mapCategory( newMenuItemDto.getCategory() ) );
        }
        menuitem.setRestaurant( restaurant );
        menuitem.setIsAvailable( true );

        return menuitem;
    }
}
