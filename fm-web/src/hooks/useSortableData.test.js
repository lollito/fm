import { renderHook, act } from '@testing-library/react';
import useSortableData from './useSortableData';

describe('useSortableData', () => {
    const mockData = [
        { id: 1, name: 'Charlie', age: 30, details: { score: 85 } },
        { id: 2, name: 'Alice', age: 25, details: { score: 95 } },
        { id: 3, name: 'Bob', age: 35, details: { score: 90 } },
    ];

    test('initializes with default data and no sort config', () => {
        const { result } = renderHook(() => useSortableData(mockData));

        expect(result.current.items).toEqual(mockData);
        expect(result.current.sortConfig).toBeNull();
    });

    test('initializes with sort config', () => {
        const config = { key: 'name', direction: 'ascending' };
        const { result } = renderHook(() => useSortableData(mockData, config));

        expect(result.current.items).toEqual([
            { id: 2, name: 'Alice', age: 25, details: { score: 95 } },
            { id: 3, name: 'Bob', age: 35, details: { score: 90 } },
            { id: 1, name: 'Charlie', age: 30, details: { score: 85 } },
        ]);
        expect(result.current.sortConfig).toEqual(config);
    });

    test('sorts data in ascending order by string key', () => {
        const { result } = renderHook(() => useSortableData(mockData));

        act(() => {
            result.current.requestSort('name');
        });

        expect(result.current.items).toEqual([
            { id: 2, name: 'Alice', age: 25, details: { score: 95 } },
            { id: 3, name: 'Bob', age: 35, details: { score: 90 } },
            { id: 1, name: 'Charlie', age: 30, details: { score: 85 } },
        ]);
        expect(result.current.sortConfig).toEqual({ key: 'name', direction: 'ascending' });
    });

    test('sorts data in descending order when requesting sort on same key', () => {
        const { result } = renderHook(() => useSortableData(mockData));

        act(() => {
            result.current.requestSort('name');
        });
        act(() => {
            result.current.requestSort('name');
        });

        expect(result.current.items).toEqual([
            { id: 1, name: 'Charlie', age: 30, details: { score: 85 } },
            { id: 3, name: 'Bob', age: 35, details: { score: 90 } },
            { id: 2, name: 'Alice', age: 25, details: { score: 95 } },
        ]);
        expect(result.current.sortConfig).toEqual({ key: 'name', direction: 'descending' });
    });

    test('sorts data by numeric key', () => {
        const { result } = renderHook(() => useSortableData(mockData));

        act(() => {
            result.current.requestSort('age');
        });

        expect(result.current.items).toEqual([
            { id: 2, name: 'Alice', age: 25, details: { score: 95 } },
            { id: 1, name: 'Charlie', age: 30, details: { score: 85 } },
            { id: 3, name: 'Bob', age: 35, details: { score: 90 } },
        ]);
    });

    test('sorts data by nested key', () => {
        const { result } = renderHook(() => useSortableData(mockData));

        act(() => {
            result.current.requestSort('details.score');
        });

        expect(result.current.items).toEqual([
            { id: 1, name: 'Charlie', age: 30, details: { score: 85 } },
            { id: 3, name: 'Bob', age: 35, details: { score: 90 } },
            { id: 2, name: 'Alice', age: 25, details: { score: 95 } },
        ]);
    });

    test('handles case-insensitive sorting', () => {
        // 'B' (66) < 'a' (97) in ASCII, so case-sensitive sort would be ['Bob', 'alice']
        // Case-insensitive sort should be ['alice', 'Bob'] because 'a' < 'b'
        const mixedCaseData = [
            { name: 'Bob' },
            { name: 'alice' },
        ];
        const { result } = renderHook(() => useSortableData(mixedCaseData));

        act(() => {
            result.current.requestSort('name');
        });

        expect(result.current.items).toEqual([
            { name: 'alice' },
            { name: 'Bob' },
        ]);
    });

    test('handles null and undefined values', () => {
        const dataWithNulls = [
            { val: 'b' },
            { val: null },
            { val: 'a' },
            { val: undefined },
        ];
        const { result } = renderHook(() => useSortableData(dataWithNulls));

        act(() => {
            result.current.requestSort('val');
        });

        // The hook implementation converts null/undefined to empty string ''
        // '' < 'a' < 'b', so null/undefined come first in ascending order
        expect(result.current.items).toEqual([
            { val: null },
            { val: undefined },
            { val: 'a' },
            { val: 'b' },
        ]);
    });

    test('handles empty list', () => {
        const { result } = renderHook(() => useSortableData([]));

        act(() => {
            result.current.requestSort('name');
        });

        expect(result.current.items).toEqual([]);
    });

    test('handles null items in list', () => {
        const dataWithNullObj = [
            { id: 1, nested: { val: 2 } },
            null,
            { id: 3, nested: { val: 1 } }
        ];

        const { result } = renderHook(() => useSortableData(dataWithNullObj));

        act(() => {
            result.current.requestSort('nested.val');
        });

        expect(result.current.items).toEqual([
            null,
            { id: 3, nested: { val: 1 } },
            { id: 1, nested: { val: 2 } }
        ]);
    });

    test('handles missing nested keys', () => {
        const data = [
            { id: 1, nested: { val: 2 } },
            { id: 2 }, // missing nested
            { id: 3, nested: { val: 1 } }
        ];

        const { result } = renderHook(() => useSortableData(data));

        act(() => {
            result.current.requestSort('nested.val');
        });

        expect(result.current.items).toEqual([
            { id: 2 },
            { id: 3, nested: { val: 1 } },
            { id: 1, nested: { val: 2 } }
        ]);
    });
});
