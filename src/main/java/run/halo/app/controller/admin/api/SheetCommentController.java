package run.halo.app.controller.admin.api;

import io.swagger.annotations.ApiOperation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.SortDefault;
import org.springframework.web.bind.annotation.*;
import run.halo.app.model.dto.BaseCommentDTO;
import run.halo.app.model.entity.SheetComment;
import run.halo.app.model.enums.CommentStatus;
import run.halo.app.model.params.CommentQuery;
import run.halo.app.model.params.SheetCommentParam;
import run.halo.app.model.vo.BaseCommentVO;
import run.halo.app.model.vo.BaseCommentWithParentVO;
import run.halo.app.model.vo.SheetCommentWithSheetVO;
import run.halo.app.service.OptionService;
import run.halo.app.service.SheetCommentService;

import javax.validation.Valid;
import java.util.List;

import static org.springframework.data.domain.Sort.Direction.DESC;

/**
 * Sheet comment controller.
 *
 * @author johnniang
 * @author ryanwang
 * @date 2019-04-25
 */
@RestController
@RequestMapping("/api/admin/sheets/comments")
public class SheetCommentController {

    private final SheetCommentService sheetCommentService;

    private final OptionService optionService;

    public SheetCommentController(SheetCommentService sheetCommentService,
                                  OptionService optionService) {
        this.sheetCommentService = sheetCommentService;
        this.optionService = optionService;
    }

    @GetMapping
    public Page<SheetCommentWithSheetVO> pageBy(@PageableDefault(sort = "updateTime", direction = DESC) Pageable pageable,
                                                CommentQuery commentQuery) {
        Page<SheetComment> sheetCommentPage = sheetCommentService.pageBy(commentQuery, pageable);
        return sheetCommentService.convertToWithSheetVo(sheetCommentPage);
    }

    @GetMapping("latest")
    public List<SheetCommentWithSheetVO> listLatest(@RequestParam(name = "top", defaultValue = "10") int top,
                                                    @RequestParam(name = "status", required = false) CommentStatus status) {
        Page<SheetComment> sheetCommentPage = sheetCommentService.pageLatest(top, status);
        return sheetCommentService.convertToWithSheetVo(sheetCommentPage.getContent());
    }

    @GetMapping("{sheetId:\\d+}/tree_view")
    @ApiOperation("Lists comments with tree view")
    public Page<BaseCommentVO> listCommentTree(@PathVariable("sheetId") Integer sheetId,
                                               @RequestParam(name = "page", required = false, defaultValue = "0") int page,
                                               @SortDefault(sort = "createTime", direction = DESC) Sort sort) {
        return sheetCommentService.pageVosBy(sheetId, PageRequest.of(page, optionService.getCommentPageSize(), sort));
    }

    @GetMapping("{sheetId:\\d+}/list_view")
    @ApiOperation("Lists comment with list view")
    public Page<BaseCommentWithParentVO> listComments(@PathVariable("sheetId") Integer sheetId,
                                                      @RequestParam(name = "page", required = false, defaultValue = "0") int page,
                                                      @SortDefault(sort = "createTime", direction = DESC) Sort sort) {
        return sheetCommentService.pageWithParentVoBy(sheetId, PageRequest.of(page, optionService.getCommentPageSize(), sort));
    }

    @PostMapping
    @ApiOperation("Creates a comment (new or reply)")
    public BaseCommentDTO createBy(@RequestBody SheetCommentParam commentParam) {
        SheetComment createdComment = sheetCommentService.createBy(commentParam);
        return sheetCommentService.convertTo(createdComment);
    }

    @PutMapping("{commentId:\\d+}/status/{status}")
    @ApiOperation("Updates comment status")
    public BaseCommentDTO updateStatusBy(@PathVariable("commentId") Long commentId,
                                         @PathVariable("status") CommentStatus status) {
        // Update comment status
        SheetComment updatedSheetComment = sheetCommentService.updateStatus(commentId, status);
        return sheetCommentService.convertTo(updatedSheetComment);
    }

    @DeleteMapping("{commentId:\\d+}")
    @ApiOperation("Deletes comment permanently and recursively")
    public BaseCommentDTO deleteBy(@PathVariable("commentId") Long commentId) {
        SheetComment deletedSheetComment = sheetCommentService.removeById(commentId);
        return sheetCommentService.convertTo(deletedSheetComment);
    }

    @GetMapping("{commentId:\\d+}")
    @ApiOperation("Gets a post comment by comment id")
    public SheetCommentWithSheetVO getBy(@PathVariable("commentId") Long commentId) {
        SheetComment comment = sheetCommentService.getById(commentId);
        return sheetCommentService.convertToWithSheetVo(comment);
    }

    @PutMapping("{commentId:\\d+}")
    public BaseCommentDTO updateBy(@Valid @RequestBody SheetCommentParam commentParam,
                                   @PathVariable("commentId") Long commentId) {
        SheetComment commentToUpdate = sheetCommentService.getById(commentId);

        commentParam.update(commentToUpdate);

        return sheetCommentService.convertTo(sheetCommentService.update(commentToUpdate));
    }
}
