package com.example.secondhand.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

/**
 * <h2>Category</h2>
 * <p>
 * موجودیت (Entity) نگاشت‌شده به جدول {@code categories}، نماینده‌ی یک
 * <b>دسته‌بندی سلسله‌مراتبی (درختی)</b> آگهی‌ها. هر دسته‌بندی می‌تواند یک
 * دسته‌بندی والد ({@link #parent}) و چندین زیردسته ({@link #children}) داشته باشد.
 * </p>
 *
 * @author تیم بک‌اند
 * @see com.example.secondhand.service.CategoryService
 */
@Entity
@Table(name = "categories")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Category {

    /** شناسه یکتای دسته‌بندی (کلید اصلی، تولید خودکار). */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** نام دسته‌بندی؛ باید در سامانه یکتا باشد. */
    @Column(nullable = false, unique = true)
    private String name;

    /** دسته‌بندی والد این دسته؛ در صورت {@code null} بودن، این دسته یک دسته‌بندی ریشه است. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Category parent;

    /** لیست زیردسته‌های مستقیم این دسته‌بندی. */
    @OneToMany(mappedBy = "parent")
    @Builder.Default
    private List<Category> children = new ArrayList<>();
    
    /** وضعیت فعال/غیرفعال بودن دسته‌بندی؛ به‌طور پیش‌فرض {@code true} است. */
    @Column(nullable = false, columnDefinition = "boolean default true")
    @Builder.Default
    private boolean active = true;
}
