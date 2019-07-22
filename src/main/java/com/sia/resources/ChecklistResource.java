/**
 * 
 */
package com.sia.resources;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.google.cloud.storage.Acl;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;

/**
 * @author quattro1
 *
 */
@RestController
@RequestMapping("/v1/checklist")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class ChecklistResource {
	
	private static final Logger log = Logger.getLogger(ChecklistResource.class.getName());
	private Storage storage;
	
	{
		try {
			// Instance Cloud Storage
			storage = StorageOptions.getDefaultInstance().getService();
		} finally {
			// Nothing really to do here.
		}
	}

	@GetMapping
	public ResponseEntity<String> lista() {
		log.warning("mi mensaje desde Spring Boot");
		String result = "";
		
		try {
			URL url = new URL("https://storage.googleapis.com/siams-ei-resources/reporte-fotografico/importaciones/checklist_banorte_layout.xlsx");		
			InputStream _is = url.openStream();	
			
			// https://www.codejava.net/coding/java-example-to-update-existing-excel-files-using-apache-poi
			// creamos el libro
			Workbook workbook = WorkbookFactory.create(_is);
			
			// cramos la hoja
			Sheet sheet = workbook.getSheetAt(0);
			
			// modificamos una celda
			Cell cellUpdate = sheet.getRow(5).getCell(2);
			cellUpdate.setCellValue("Editado desde Java");
			
			_is.close();
			
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			workbook.write(baos);
			workbook.close();
			
			InputStream is = new ByteArrayInputStream(baos.toByteArray());
			
			// guardar en cloud storage
			List<Acl> acls = new ArrayList<>();
			acls.add(Acl.of(Acl.User.ofAllUsers(), Acl.Role.READER));
			BlobId blobId = BlobId.of("siams-ei-resources", "reporte-fotografico/importaciones/checklist_banorte_layout_" + System.currentTimeMillis() + ".xlsx");
			BlobInfo blobInfo = BlobInfo.newBuilder(blobId).setAcl(acls)
					.build();
			Blob blob = storage.create(blobInfo, is);
			
			result = "works fine woorkbook=" + workbook.toString();
		} catch(Exception e) {
			log.warning(e.getMessage());
			result = e.getMessage();
		}
		
		return new ResponseEntity<>("la respuesta desde Spring Boot!! result=" + result, HttpStatus.OK);
	}
}
