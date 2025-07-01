package com.group3.askmyfriend.repository;

import com.group3.askmyfriend.entity.CommentEntity;
import com.group3.askmyfriend.entity.PostEntity;
import com.group3.askmyfriend.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<CommentEntity, Long> {
    
    // 기존 메서드들
    List<CommentEntity> findByPost(PostEntity post);
    
    // 🔥 누락된 메서드들 추가
    List<CommentEntity> findByAuthor(UserEntity author);
    
    long countByPost(PostEntity post);
    
    // 추가 유용한 메서드들
    List<CommentEntity> findByPostOrderByCreatedAtAsc(PostEntity post);
    
    List<CommentEntity> findByPostOrderByCreatedAtDesc(PostEntity post);
    
    List<CommentEntity> findByAuthorOrderByCreatedAtDesc(UserEntity author);
    
    void deleteByPost(PostEntity post);
    
    void deleteByAuthor(UserEntity author);
    
    // ⭐️ 마이페이지용 메서드 (SQL 오류 해결)
    @Query("SELECT DISTINCT c.post FROM CommentEntity c WHERE c.author = :author ORDER BY c.post.createdAt DESC")
    List<PostEntity> findPostsByReplierOrderByCreatedDateDesc(@Param("author") UserEntity author);
    
    // ⭐️ 안전한 방법: ORDER BY 없이 조회 (Java에서 정렬)
    @Query("SELECT DISTINCT c.post FROM CommentEntity c WHERE c.author = :author")
    List<PostEntity> findDistinctPostsByAuthor(@Param("author") UserEntity author);
    
    // ⭐️ 특정 사용자가 댓글을 단 게시물 목록 (중복 제거)
    @Query("SELECT DISTINCT c.post FROM CommentEntity c WHERE c.author = :author ORDER BY c.post.createdAt DESC")
    List<PostEntity> findDistinctPostsByAuthorOrderByPostCreatedAtDesc(@Param("author") UserEntity author);
    
    // ⭐️ 특정 게시물에 댓글을 단 사용자 목록
    @Query("SELECT DISTINCT c.author FROM CommentEntity c WHERE c.post = :post")
    List<UserEntity> findDistinctAuthorsByPost(@Param("post") PostEntity post);
    
    // ⭐️ 특정 사용자의 댓글 수 계산
    long countByAuthor(UserEntity author);
    
    // ⭐️ 특정 기간 내 댓글 조회 (필요시 사용)
    @Query("SELECT c FROM CommentEntity c WHERE c.author = :author AND c.createdAt >= :startDate ORDER BY c.createdAt DESC")
    List<CommentEntity> findByAuthorAndCreatedAtAfterOrderByCreatedAtDesc(
        @Param("author") UserEntity author, 
        @Param("startDate") java.time.LocalDateTime startDate
    );
    
    // ⭐️ 추가: 댓글 작성자별 게시물 조회 (서브쿼리 방식)
    @Query("SELECT p FROM PostEntity p WHERE p.id IN " +
           "(SELECT DISTINCT c.post.id FROM CommentEntity c WHERE c.author = :author) " +
           "ORDER BY p.createdAt DESC")
    List<PostEntity> findPostsCommentedByAuthor(@Param("author") UserEntity author);
}
