package com.smart_housing.smart_housing.service;

import com.smart_housing.smart_housing.model.House;
import com.smart_housing.smart_housing.repository.HouseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class HouseService {

    @Autowired
    private HouseRepository houseRepository;

    public List<House> getAllHouses() {
        return houseRepository.findAll();
    }

    public House getHouseById(int id) {
        return houseRepository.findById(id).orElse(null);
    }

    public House addHouse(House house) {
        return houseRepository.save(house);
    }

    public String deleteHouse(int id) {
        if (houseRepository.existsById(id)) {
            houseRepository.deleteById(id);
            return "House deleted successfully.";
        }
        return "House not found.";
    }

    public House updateHouse(int id, House updatedHouse) {
        House house = houseRepository.findById(id).orElse(null);
        if (house != null) {
            house.setLandlordId(updatedHouse.getLandlordId());
            house.setLocation(updatedHouse.getLocation());
            house.setRent(updatedHouse.getRent());
            house.setAmenities(updatedHouse.getAmenities());
            house.setImage(updatedHouse.getImage());
            house.setImagePath(updatedHouse.getImagePath());
            house.setStatus(updatedHouse.getStatus());
            return houseRepository.save(house);
        }
        return null;
    }
}
