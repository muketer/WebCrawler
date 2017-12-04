package com.muketer.PotalWebCrawler.ServiceClass;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.muketer.PotalWebCrawler.DAOInterface.SearchInformationDAO;
import com.muketer.PotalWebCrawler.DTO.SearchInformationDTO;
import com.muketer.PotalWebCrawler.ServiceInterface.I_TemporaryDAOUser;

@Service
public class TemporaryDAOUser implements I_TemporaryDAOUser{

	@Autowired
	private SearchInformationDAO dao;
	
	@Override
	public void dataInsert(SearchInformationDTO dto) {
		dao.dataInsert(dto);
	}

}
