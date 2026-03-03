package com.hrm.leave.mapper;

import com.hrm.leave.dto.response.ResultPaginationDTO;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PaginationMapper {

    public ResultPaginationDTO convertToResultPaginationDTO(int pageNumber, int pageSize, int totalPages, long totalElements, List<?> list) {
        ResultPaginationDTO rs = new ResultPaginationDTO();
        ResultPaginationDTO.Meta mt = new ResultPaginationDTO.Meta();

        mt.setPage(pageNumber);
        mt.setPageSize(pageSize);
        mt.setPages(totalPages);
        mt.setTotal(totalElements);

        rs.setMeta(mt);
        rs.setResult(list);
        return rs;

    }
}
