import React, {useState} from 'react';
import {
    Pagination,
    PaginationContent,
    PaginationEllipsis,
    PaginationItem,
    PaginationLink,
    PaginationNext,
    PaginationPrevious,
} from "@/components/ui/pagination";

interface PaginatedCardListProps<T> {
    // Component props
    CardComponent: React.ComponentType<T>;
    items?: T[];

    // Pagination props
    itemsPerPage?: number;

    // Styling props
    className?: string;
    gridClassName?: string;
}

export const PaginatedCardList = ({
                                      CardComponent,
                                      items,
                                      itemsPerPage = 6,
                                      className = "",
                                      gridClassName = "grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4 mb-8"
                                  }: PaginatedCardListProps<any>) => {

    const allItems = items ? items : [];
    const [currentPage, setCurrentPage] = useState(1);
    const totalPages = Math.ceil(allItems.length / itemsPerPage);
    const currentItems = allItems.slice(
        (currentPage - 1) * itemsPerPage,
        currentPage * itemsPerPage
    );

    const getPageNumbers = () => {
        const pages = [];
        for (let i = 1; i <= totalPages; i++) {
            if (
                i === 1 ||
                i === totalPages ||
                (i >= currentPage - 1 && i <= currentPage + 1)
            ) {
                pages.push(i);
            } else if (i === currentPage - 2 || i === currentPage + 2) {
                pages.push('...');
            }
        }
        // @ts-ignore
        return [...new Set(pages)];
    };

    const handlePageChange = (page: any) => {
        setCurrentPage(page);
    };

    return (
        <div className={`w-full my-3 mx-auto space-y-6 ${className}`}>

            {/* Cards Grid */}
            <div className={gridClassName}>
                {currentItems.map((item: any, index: number) => (
                    <CardComponent key={index} {...item} />
                ))}
            </div>

            {/* Pagination */}
            {totalPages > 1 && (
                <Pagination>
                    <PaginationContent>
                        <PaginationItem>
                            <PaginationPrevious
                                onClick={() => currentPage > 1 && handlePageChange(currentPage - 1)}
                                className={currentPage === 1 ? "pointer-events-none opacity-50" : "cursor-pointer"}
                            />
                        </PaginationItem>

                        {getPageNumbers().map((page, index) => (
                            <PaginationItem key={index}>
                                {page === '...' ? (
                                    <PaginationEllipsis/>
                                ) : (
                                    <PaginationLink
                                        onClick={() => handlePageChange(page)}
                                        isActive={currentPage === page}
                                        className="cursor-pointer"
                                    >
                                        {page}
                                    </PaginationLink>
                                )}
                            </PaginationItem>
                        ))}

                        <PaginationItem>
                            <PaginationNext
                                onClick={() => currentPage < totalPages && handlePageChange(currentPage + 1)}
                                className={currentPage === totalPages ? "pointer-events-none opacity-50" : "cursor-pointer"}
                            />
                        </PaginationItem>
                    </PaginationContent>
                </Pagination>
            )}
        </div>
    );
};
