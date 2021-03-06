package hu.bme.mit.textmine.rdf.web;

import java.nio.charset.StandardCharsets;
import java.util.List;

import org.eclipse.rdf4j.query.MalformedQueryException;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import hu.bme.mit.textmine.mongo.document.model.Line;
import hu.bme.mit.textmine.mongo.document.model.QueryHits;
import hu.bme.mit.textmine.mongo.document.model.Section;
import hu.bme.mit.textmine.rdf.model.SpatialQueryRequest;
import hu.bme.mit.textmine.rdf.model.SpatialQueryResult;
import hu.bme.mit.textmine.rdf.service.LocationService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

@Api(value = "/locations")
@RestController
@RequestMapping(produces = MediaType.APPLICATION_JSON_UTF8_VALUE, path = "/locations")
public class LocationController {

    @Autowired
    private LocationService service;

    private static final class LineHits extends QueryHits<Line> {
    }

    private static final class SectionHits extends QueryHits<Section> {
    }

    @ApiOperation(
            value = "Check if location exists",
            httpMethod = "GET",
            response = Boolean.class)
    @RequestMapping(method = RequestMethod.GET, path = "/exists")
    public ResponseEntity<Boolean> exists(
            @ApiParam(
                    value = "Location name",
                    required = true,
                    name = "name") @RequestParam("name") String locationName)
            throws RepositoryException, MalformedQueryException, Exception {
        return new ResponseEntity<>(this.service.foundInDbpedia(locationName), HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.POST, produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public @ResponseBody ResponseEntity<byte[]> postLocations(MultipartHttpServletRequest request) {
        MultipartFile file = request.getFile("file");
        String content = this.service.queryResources(file);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment;filename=\"location_links.csv\"");
        headers.add("Content-Type", "text/csv;charset=UTF-8");
        return new ResponseEntity<>(content.getBytes(StandardCharsets.UTF_8), headers, HttpStatus.OK);
    }

    @ApiOperation(
            value = "Get locations in vicinity of IRI",
            httpMethod = "POST",
            response = SpatialQueryResult.class,
            responseContainer = "List")
    @RequestMapping(method = RequestMethod.POST, path = "/geo-near")
    public ResponseEntity<List<SpatialQueryResult>> spatialQuery(
            @ApiParam(value = "Spatial request", required = true) @RequestBody SpatialQueryRequest request) {
        List<SpatialQueryResult> result = this.service.geoNearQuery(request.getIri(), request.getRadius());
        if (result.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @ApiOperation(
            value = "Get lines in connection with a Country or Location",
            httpMethod = "GET",
            response = LineHits.class)
    @RequestMapping(method = RequestMethod.GET, path = "/lines")
    public ResponseEntity<QueryHits<Line>> lineQuery(
            @ApiParam(value = "Document id", required = false, name = "documentId") @RequestParam(
                    name = "documentId",
                    required = false) String documentId,
            @ApiParam(value = "Section number", required = false, name = "sectionSerial") @RequestParam(
                    name = "sectionSerial",
                    required = false) Integer sectionSerial,
            @ApiParam(value = "Location name", required = false, name = "location") @RequestParam(
                    required = false,
                    name = "location") String locationName,
            @ApiParam(value = "Country name", required = false, name = "country") @RequestParam(
                    required = false,
                    name = "country") String countryName) {
        if (locationName != null && countryName != null || locationName == null && countryName == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        QueryHits<Line> result = null;
        if (locationName != null) {
            result = this.service.linesForLocation(documentId, sectionSerial, locationName);
        } else {
            result = this.service.linesForCountry(documentId, sectionSerial, countryName);
        }
        if (result.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @ApiOperation(
            value = "Get sections in connection with a Location",
            httpMethod = "GET",
            response = SectionHits.class)
    @RequestMapping(method = RequestMethod.GET, path = "/sections")
    public ResponseEntity<QueryHits<Section>> sectionQuery(
            @ApiParam(value = "Document id", required = false, name = "documentId") @RequestParam(
                    name = "documentId",
                    required = false) String documentId,
            @ApiParam(
                    value = "Location name",
                    required = true,
                    name = "location") @RequestParam("location") String locationName) {
        QueryHits<Section> result = this.service.sectionsForLocation(documentId, locationName);
        if (result.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @ApiOperation(
            value = "Get sections in connection with a Location",
            httpMethod = "POST",
            response = SpatialQueryResult.class,
            responseContainer = "List")
    @RequestMapping(method = RequestMethod.POST, path = "/sections/geo-near")
    public ResponseEntity<List<SpatialQueryResult>> sectionSpatialQuery(
            @ApiParam(value = "Spatial request", required = true) @RequestBody SpatialQueryRequest request) {
        List<SpatialQueryResult> result = this.service.sectionLocationVicinity(request.getIri(), request.getRadius());
        if (result.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @ApiOperation(
            value = "Get pages in connection with a Location",
            httpMethod = "GET",
            response = Section.class,
            responseContainer = "List")
    @RequestMapping(method = RequestMethod.GET, path = "/pages")
    public ResponseEntity<List<Section>> pagesQuery(
            @ApiParam(value = "Document id", required = true, name = "documentId") @RequestParam(
                    name = "documentId",
                    required = false) String documentId,
            @ApiParam(
                    value = "Location name",
                    required = true,
                    name = "location") @RequestParam("location") String locationName) {
        List<Section> result = this.service.pagesForLocation(documentId, locationName);
        if (result.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(result, HttpStatus.OK);
    }
}
